package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int NUMBER_OF_DSP_NODES = 1;

    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private DspClusterStampVoteCrypto dspClusterStampVoteCrypto;
    @Autowired
    private SourceStarvationService sourceStarvationService;
    @Autowired
    private BaseNodeBalanceService balanceService;
    @Autowired
    private ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;

    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;

    private ClusterStampData currentClusterStamp;

    @PostConstruct
    private void init() {
        isReadyForClusterStamp = false;
        currentClusterStamp = new ClusterStampData();
    }

    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        log.debug("Start preparation of ZS for cluster stamp");
        propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        CompletableFuture.runAsync(this::initTimer);
    }

    private void initTimer() {
        try {
            Thread.sleep(replyTimeOut);
            if(!isReadyForClusterStamp) {
                log.info("Zero spend started cluster stamp after timer has expired.");
                isReadyForClusterStamp = true;
                makeAndPropagateClusterStamp();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public void handleDspNodeReadyForClusterStampMessage(DspReadyForClusterStampData dspReadyForClusterStampData) {

        log.debug("\'Ready for cluster stamp\' propagated message received from DSP to ZS");
        if(!isReadyForClusterStamp && clusterStampStateCrypto.verifySignature(dspReadyForClusterStampData)) {

            if(currentClusterStamp.getDspReadyForClusterStampDataList().contains(dspReadyForClusterStampData)) {
                log.warn("\'Dsp Node Ready For Cluster Stamp\' was already sent by the sender of this message");
                return;
            }
            currentClusterStamp.getDspReadyForClusterStampDataList().add(dspReadyForClusterStampData);

            if ( currentClusterStamp.getDspReadyForClusterStampDataList().size() == NUMBER_OF_DSP_NODES ) {
                log.info("Zero spend starting cluster stamp, after all dsp nodes are ready for cluster stamp");
                makeAndPropagateClusterStamp();
            }
        }
    }

    public void makeAndPropagateClusterStamp() {

        dspVoteService.stopSumAndSaveVotes();
        sourceStarvationService.stopCheckSourcesStarvation();

        ClusterStampData clusterStampData = currentClusterStamp;
        clusterStampData.setBalanceMap(balanceService.getBalanceMap());
        clusterStampData.setUnconfirmedTransactions(getUnconfirmedTransactions());
        setHash(clusterStampData);

        clusterStampCrypto.signMessage(clusterStampData);
        propagationPublisher.propagate(clusterStampData, Arrays.asList(NodeType.DspNode));

        currentClusterStamp = new ClusterStampData();
        clusterStamps.put(clusterStampData);
        log.info("Restart DSP vote service to sum and save DSP votes, and starvation service");
    }

    public void handleDspClusterStampVote(DspClusterStampVoteData dspClusterStampVoteData) {

        if(dspClusterStampVoteCrypto.verifySignature(dspClusterStampVoteData)) {

            ClusterStampData clusterStampData = clusterStamps.getByHash(dspClusterStampVoteData.getHash());
            clusterStampData.getClusterStampConsensusResult().getDspClusterStampVoteDataList().add(dspClusterStampVoteData);

            int validClusterStampVotes = 0;
            for(DspClusterStampVoteData dspClusterStampVoteDataIterator : clusterStampData.getClusterStampConsensusResult().getDspClusterStampVoteDataList()) {
                if( dspClusterStampVoteDataIterator.isValidClusterStamp) {
                    validClusterStampVotes++;
                }
            }

            if(validClusterStampVotes == NUMBER_OF_DSP_NODES) {
                clusterStampData.getClusterStampConsensusResult().setDspConsensus(true);
                clusterStampConsensusResultCrypto.signMessage(clusterStampData.getClusterStampConsensusResult());

                propagationPublisher.propagate(clusterStampData.getClusterStampConsensusResult(), Arrays.asList(NodeType.DspNode));
                this.isReadyForClusterStamp = false;

                dspVoteService.startSumAndSaveVotes();
                sourceStarvationService.startCheckSourcesStarvation();
            }

            clusterStamps.put(clusterStampData);
        }
    }
}