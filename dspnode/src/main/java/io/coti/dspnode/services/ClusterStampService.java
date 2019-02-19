package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspReadyForClusterStamp;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService implements IClusterStampService{

    private static final int NUMBER_OF_FULL_NODES = 1;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private DspReadyForClusterStamp dspReadyForClusterStamp;
    @Autowired
    private ISender sender;
    @Autowired
    private BaseNodeBalanceService balanceService;
    @Autowired
    private DspClusterStampVoteCrypto dspClusterStampVoteCrypto;
    @Autowired
    private IValidationService validationService;
    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;
    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;
    private int readyForClusterStampMsgCount;
    private ClusterStampState clusterStampState;


    @Override
    @PostConstruct
    public void init() {
        clusterStampState = ClusterStampState.OFF;
        readyForClusterStampMsgCount = 0;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        if(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationData, clusterStampState)){
            log.debug("Start preparation of DSP for cluster stamp");
            clusterStampState = clusterStampState.nextState(); //Change state to PREPARING

            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
            CompletableFuture.runAsync(this::initReadyForClusterStampTimer);
        }
    }

    @Override
    public boolean isClusterStampInProcess(){

        return clusterStampState == ClusterStampState.IN_PROCESS;
    }

    @Override
    public boolean isClusterStampPreparing() {

        return clusterStampState == ClusterStampState.PREPARING;
    }

    @Override
    public boolean isClusterStampReady(){

        return clusterStampState == ClusterStampState.READY;
    }

    private void initReadyForClusterStampTimer(){
        try {
            Thread.sleep(replyTimeOut);
            if(!isClusterStampInProcess()) {
                log.info("DSP sending it's ready after timer expired");
                sendDspReadyForClusterStamp(new DspReadyForClusterStampData());
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public void handleFullNodeReadyForClusterStampMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {

        log.debug("\'Ready for cluster stamp\' propagated message received from FN to DSP");
        if(validationService.validateReadyForClusterStampRequest(fullNodeReadyForClusterStampData,clusterStampState)) {
            DspReadyForClusterStampData dspReadyForClusterStampData = dspReadyForClusterStamp.getByHash(fullNodeReadyForClusterStampData.getHash());

            if (dspReadyForClusterStampData == null) {
                dspReadyForClusterStampData = new DspReadyForClusterStampData(fullNodeReadyForClusterStampData.getTotalConfirmedTransactionsCount());
            }
            else {
                if(dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().contains(fullNodeReadyForClusterStampData)){
                    log.warn("\'Full Node Ready For Cluster Stamp\' was already sent by the sender of this message");
                    return;
                }
            }

            dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().add(fullNodeReadyForClusterStampData);
            dspReadyForClusterStamp.put(dspReadyForClusterStampData);
            readyForClusterStampMsgCount++;

            if(NUMBER_OF_FULL_NODES == readyForClusterStampMsgCount) {
                log.info("All full nodes are ready for cluster stamp");
                sendDspReadyForClusterStamp(dspReadyForClusterStampData);
            }
        }
    }

    public void voteAndStoreClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            ClusterStampData clusterStampDataLocal = new ClusterStampData();
            clusterStampDataLocal.setBalanceMap(balanceService.getBalanceMap());
            clusterStampDataLocal.setUnconfirmedTransactions(getUnconfirmedTransactions());
            setHash(clusterStampDataLocal);

            boolean validClusterStamp = clusterStampData.getHash().equals(clusterStampDataLocal.getHash());
            DspClusterStampVoteData dspClusterStampVoteData = new DspClusterStampVoteData(clusterStampData.getHash(), validClusterStamp);

            dspClusterStampVoteCrypto.signMessage(dspClusterStampVoteData);
            sender.send(dspClusterStampVoteData, receivingZerospendAddress);

            if(validClusterStamp) {
                clusterStamps.put(clusterStampData);
            }
        }
    }

    private void sendDspReadyForClusterStamp(DspReadyForClusterStampData dspReadyForClusterStampData) {
        clusterStampState = clusterStampState.nextState(); //Change state to READY
        clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
        propagationPublisher.propagate(dspReadyForClusterStampData, Arrays.asList(NodeType.FullNode));
        sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
        readyForClusterStampMsgCount = 0;
    }

    public void handleZeroSpendIsReadyForClusterStampData(ZeroSpendIsReadyForClusterStampData zerospendIsReadyForClusterStampData){
        if(clusterStampStateCrypto.verifySignature(zerospendIsReadyForClusterStampData)) {
            clusterStampState = clusterStampState.nextState(); //Change state to IN_PROCESS
            //TODO 2/18/2019 astolia: need to handle anything here?
        }
    }

    @Override
    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
        super.handleClusterStampConsensusResult(clusterStampConsensusResult);
        propagationPublisher.propagate(clusterStampConsensusResult, Arrays.asList(NodeType.FullNode));
    }

    //TODO 2/18/2019 astolia: when to change dsp cluster stamp state to off?
}