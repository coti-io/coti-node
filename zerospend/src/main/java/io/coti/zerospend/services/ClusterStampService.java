package io.coti.zerospend.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeClusterStampService;
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
public class ClusterStampService extends BaseNodeClusterStampService implements IClusterStampService {

    private static final int NUMBER_OF_DSP_NODES = 1;

    @Autowired
    private DspClusterStampVoteCrypto dspClusterStampVoteCrypto;
    @Autowired
    private SourceStarvationService sourceStarvationService;
    @Autowired
    private BaseNodeBalanceService balanceService;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private IValidationService validationService;
    //TODO BUG dspVoteService appears also in the extended class. either add method to interface or remove form base class.
    @Autowired
    private DspVoteService dspVoteService;
    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;
    private ClusterStampData currentClusterStamp;
    private long totalConfirmedTransactionsCount;
    private ClusterStampState clusterStampState;


    @Override
    @PostConstruct
    public void init() {
        clusterStampState = ClusterStampState.OFF;
        currentClusterStamp = new ClusterStampData();
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        if(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationData, clusterStampState)){
            log.debug("Start preparation of ZS for cluster stamp");
            totalConfirmedTransactionsCount = clusterStampPreparationData.getTotalConfirmedTransactionsCount();
            clusterStampState = clusterStampState.nextState(); //Change state to PREPARING
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
            CompletableFuture.runAsync(this::initPrepareForClusterStampTimer);
        }
    }

    @Override
    public boolean isClusterStampInProcess(){

        return clusterStampState == ClusterStampState.IN_PROCESS;
    }

    @Override
    public boolean isClusterStampPreparing(){

        return clusterStampState == ClusterStampState.PREPARING;
    }

    @Override
    public boolean isClusterStampReady(){

        return clusterStampState == ClusterStampState.READY;
    }

    private void initPrepareForClusterStampTimer() {
        try {
            Thread.sleep(replyTimeOut);
            if(!isClusterStampInProcess()) {
                log.info("Zero spend started cluster stamp after timer has expired.");
                makeAndPropagateClusterStamp();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public void handleDspNodeReadyForClusterStampMessage(DspReadyForClusterStampData dspReadyForClusterStampData) {

        log.debug("\'Ready for cluster stamp\' propagated message received from DSP to ZS");
        //TODO 2/19/2019 astolia: change to use validator.
        if(isClusterStampPreparing() && clusterStampStateCrypto.verifySignature(dspReadyForClusterStampData)) {

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

    /**
     * Creates and propagates cluster stamp.
     * Called after received 'dsp ready' from all DSP's OR after prepare for clusterstamp timeout has expired.
     */
    public void makeAndPropagateClusterStamp() {
        clusterStampState = clusterStampState.nextState(); //Change state to READY
        //TODO BUG dspVoteService appears also in the extended class. either add method to interface or remove form base class.
        dspVoteService.stopSumAndSaveVotes();
        sourceStarvationService.stopCheckSourcesStarvation();
        propagateClusterStampInProcessData();

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
                clusterStampState = clusterStampState.nextState(); //Change state to OFF

                dspVoteService.startSumAndSaveVotes();
                sourceStarvationService.startCheckSourcesStarvation();
            }

            clusterStamps.put(clusterStampData);
        }
    }

    private void propagateClusterStampInProcessData() {
        ZeroSpendIsReadyForClusterStampData zerospendIsReadyForClusterStampData = new ZeroSpendIsReadyForClusterStampData(totalConfirmedTransactionsCount);
        zerospendIsReadyForClusterStampData.setDspReadyForClusterStampDataList(currentClusterStamp.getDspReadyForClusterStampDataList());
        propagationPublisher.propagate(zerospendIsReadyForClusterStampData, Arrays.asList(NodeType.DspNode));
        clusterStampState = clusterStampState.nextState(); //Change state to IN_PROCESS
    }
}