package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
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

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int DSP_NODES_MAJORITY = 1;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private SourceStarvationService sourceStarvationService;
    @Autowired
    private BaseNodeBalanceService balanceService;
    @Autowired
    private TccConfirmationService tccConfirmationService;
    @Autowired
    private ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ClusterStamp clusterStamp;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;
    private ClusterStampData currentClusterStamp;
    private boolean isClusterStampInProgress;



    @PostConstruct
    private void init() {
        isClusterStampInProgress = false;
        currentClusterStamp = new ClusterStampData(new Hash("inProgress"));
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        log.debug("Start preparation of ZS for cluster stamp");
        propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        CompletableFuture.runAsync(this::initTimer);
    }

    private void initTimer(){
        try {
            Thread.sleep(replyTimeOut);
            if(!isClusterStampInProgress){
                log.info("Zero spend starting cluster stamp after timer expired.");
                isClusterStampInProgress = true;
                //TODO 2/12/2019 astolia: Start cluster stamp
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void handleDspNodeReadyForClusterStampMessage(DspReadyForClusterStampData dspReadyForClusterStampData) {

        log.debug("\'Ready for cluster stamp\' propagated message received from DSP to ZS");
        if(!isClusterStampInProgress && clusterStampStateCrypto.verifySignature(dspReadyForClusterStampData)) {
            currentClusterStamp.getDspReadyForClusterStampDataList().add(dspReadyForClusterStampData);

            if ( currentClusterStamp.getDspReadyForClusterStampDataList().size() >= DSP_NODES_MAJORITY ) {
                log.info("Stop dsp vote service from sum and save dsp votes");
                //dspVoteService.stopSumAndSaveVotes();
                sourceStarvationService.stopCheckSourcesStarvation();
                makeAndPropagateClusterStamp();
            }
        }

    }

    public void makeAndPropagateClusterStamp() {

        ClusterStampData clusterStampData = currentClusterStamp;
        clusterStampData.setBalanceMap(balanceService.getBalanceMap());
        clusterStampData.setUnconfirmedTransactions(getUnconfirmedTransactions());
        clusterStampData.setHash();

        clusterStampCrypto.signMessage(clusterStampData);
        propagationPublisher.propagate(clusterStampData, Arrays.asList(NodeType.DspNode));

        currentClusterStamp = new ClusterStampData(new Hash("inProgress"));
        clusterStamp.put(clusterStampData);
        log.info("Restart DSP vote service to sum and save dsp votes, and starvation service");
        //dspVoteService.startSumAndSaveVotes();
        //sourceStarvationService.startCheckSourcesStarvation();
    }

    @Override
    public boolean isClusterStampInProgress() {
        return isClusterStampInProgress;
    }

    public void handleDspClusterStampVote(DspClusterStampVoteData dspClusterStampVoteData) {

        ClusterStampData clusterStampData = clusterStamp.getByHash(dspClusterStampVoteData.getHash());
        clusterStampData.getClusterStampConsensusResult().getDspClusterStampVoteDataList().add(dspClusterStampVoteData);

        int validClusterStampVotes = 0;
        for(DspClusterStampVoteData dspClusterStampVoteDataIterator : clusterStampData.getClusterStampConsensusResult().getDspClusterStampVoteDataList()) {
            if( dspClusterStampVoteDataIterator.validClusterStamp ) {
                validClusterStampVotes++;
            }
        }

        if(validClusterStampVotes >= DSP_NODES_MAJORITY) {
            clusterStampData.getClusterStampConsensusResult().setDspConsensus(true);
            clusterStampConsensusResultCrypto.signMessage(clusterStampData.getClusterStampConsensusResult());
            propagationPublisher.propagate(clusterStampData.getClusterStampConsensusResult(), Arrays.asList(NodeType.DspNode));
            this.isClusterStampInProgress = false;
        }

        clusterStamp.put(clusterStampData);
    }

    private List<TransactionData> getUnconfirmedTransactions() {

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