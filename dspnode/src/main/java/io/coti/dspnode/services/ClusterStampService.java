package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspReadyForClusterStamp;
import io.coti.basenode.services.TccConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Handler for PrepareForSnapshot messages propagated to DSP.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int NUMBER_OF_FULL_NODES = 1;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    private DspReadyForClusterStamp dspReadyForClusterStampMessages;
    @Autowired
    private ISender sender;
    @Autowired
    private BaseNodeBalanceService balanceService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private TccConfirmationService tccConfirmationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ClusterStamp clusterStamp;

    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;

    @Value("${clusterstamp.reply.timeout}")

    private int replyTimeOut;

    private boolean isClusterStampInProgress;

    private int readyForClusterStampMsgCount;

    @PostConstruct
    private void init() {
        isClusterStampInProgress = false;
        readyForClusterStampMsgCount = 0;
    }

    @Override
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
            if(!isClusterStampInProgress){
                log.info("DSP starting cluster stamp after timer expired.");
                isClusterStampInProgress = true;
                //TODO 2/12/2019 astolia: delete messages and reset counter to 0.
                // START CLUSTER STAMP
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private boolean validatePrepareForClusterStampRequest(ClusterStampPreparationData clusterStampPreparationData){
        if(isClusterStampInProgress){
            log.info("Preparation for cluster stamp is already in process");
            //TODO 2/4/2019 astolia: Send to ZS that snapshot prepare is in process?
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
        if(!isClusterStampInProgress && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)) {

            DspReadyForClusterStampData dspReadyForClusterStampData = dspReadyForClusterStampMessages.getByHash(fullNodeReadyForClusterStampData.getHash());

            if (dspReadyForClusterStampData == null) {
                dspReadyForClusterStampData = new DspReadyForClusterStampData(fullNodeReadyForClusterStampData.getLastDspConfirmed());
            }
            else{
                if(dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().contains(fullNodeReadyForClusterStampData)){
                    log.warn("\'Full Node Ready For Cluster Stamp\' was already sent by the sender of this message");
                    return;
                }
            }
            dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().add(fullNodeReadyForClusterStampData);
            dspReadyForClusterStampMessages.put(dspReadyForClusterStampData);
            readyForClusterStampMsgCount++;

            if(NUMBER_OF_FULL_NODES == readyForClusterStampMsgCount){
                log.info("All full nodes are ready for cluster stamp");
                clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
                sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
                dspReadyForClusterStampMessages.deleteByHash(fullNodeReadyForClusterStampData.getHash());
                readyForClusterStampMsgCount = 0;
            }
        }
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            ClusterStampData clusterStampDataLocal = new ClusterStampData();
            clusterStampDataLocal.setBalanceMap(balanceService.getBalanceMap());
            clusterStampDataLocal.setUnconfirmedTransactions(getUnconfirmedTransactions());
            clusterStampDataLocal.setHash();

            boolean validClusterStamp = clusterStampData.getHash().equals(clusterStampDataLocal.getHash());
            DspClusterStampVoteData dspClusterStampVoteData = new DspClusterStampVoteData(clusterStampData.getHash(), validClusterStamp);
            sender.send(dspClusterStampVoteData, receivingZerospendAddress);

            if(validClusterStamp) {
                clusterStamp.put(clusterStampData);
            }
        }
    }

    public void newClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {

        if(clusterStampConsensusResultCrypto.verifySignature(clusterStampConsensusResult) && clusterStampConsensusResult.isDspConsensus()) {

            ClusterStampData clusterStampData = clusterStamp.getByHash(clusterStampConsensusResult.getHash());
            clusterStampData.setClusterStampConsensusResult(clusterStampConsensusResult);
            propagationPublisher.propagate(clusterStampConsensusResult, Arrays.asList(NodeType.FullNode));
            isClusterStampInProgress = false;
        }
    }

    @Override
    public boolean isClusterStampInProgress() {
        return isClusterStampInProgress;
    }

    protected List<TransactionData> getUnconfirmedTransactions() {

        Set unreachedDspcHashTransactions = dspVoteService.getTransactionHashToVotesListMapping().keySet();
        Set unreachedTccHashTransactions = tccConfirmationService.getHashToTccUnConfirmTransactionsMapping().keySet();

        List<Hash> unconfirmedHashTransactions = new ArrayList<>();
        unconfirmedHashTransactions.addAll(unreachedDspcHashTransactions);
        unconfirmedHashTransactions.addAll(unreachedTccHashTransactions);

        List<TransactionData> unconfirmedTransactions = new ArrayList<>();
        for(Hash unconfirmedHashTransaction : unconfirmedHashTransactions) {
            unconfirmedTransactions.add(transactions.getByHash(unconfirmedHashTransaction));
        }

        return unconfirmedTransactions;
    }
}