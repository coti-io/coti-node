package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspReadyForClusterStamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Handler for PrepareForSnapshot messages propagated to DSP.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int NUMBER_OF_FULL_NODES = 1;

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

    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;

    @Value("${clusterstamp.reply.timeout}")

    private int replyTimeOut;
    private int readyForClusterStampMsgCount;

    @PostConstruct
    private void init() {
        isReadyForClusterStamp = false;
        readyForClusterStampMsgCount = 0;
    }

    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        log.debug("Prepare for cluster stamp propagated message received from ZS to DSP");
        if(validatePrepareForClusterStampRequest(clusterStampPreparationData)){
            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
            CompletableFuture.runAsync(this::initTimer);
        }
    }

    private void initTimer(){
        try {
            Thread.sleep(replyTimeOut);
            if(!isReadyForClusterStamp) {
                log.info("DSP sending it's ready after timer expired");
                sendDspReadyForClusterStamp(new DspReadyForClusterStampData());
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private boolean validatePrepareForClusterStampRequest(ClusterStampPreparationData clusterStampPreparationData){
        if(isReadyForClusterStamp){
            log.info("Preparation for cluster stamp is already in process");
            return false;
        }
        else if(!clusterStampStateCrypto.verifySignature(clusterStampPreparationData)){
            log.error("Wrong signature for \'prepare for cluster stamp\' request.");
            return false;
        }
        return true;
    }

    public void handleFullNodeReadyForClusterStampMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {

        log.debug("\'Ready for cluster stamp\' propagated message received from FN to DSP");
        if(!isReadyForClusterStamp && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)) {

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

    public void newClusterStamp(ClusterStampData clusterStampData) {

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

    public boolean isReadyForClusterStamp() {
        return isReadyForClusterStamp;
    }

    private void sendDspReadyForClusterStamp(DspReadyForClusterStampData dspReadyForClusterStampData) {

        isReadyForClusterStamp = true;
        clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
        sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
        readyForClusterStampMsgCount = 0;
    }

    @Override
    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
        super.handleClusterStampConsensusResult(clusterStampConsensusResult);
        propagationPublisher.propagate(clusterStampConsensusResult, Arrays.asList(NodeType.FullNode));
    }
}