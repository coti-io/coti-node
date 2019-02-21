package io.coti.zerospend.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final int NUMBER_OF_DSP_NODES = 1;

    @Autowired
    private DspClusterStampVoteCrypto dspClusterStampVoteCrypto;
    @Autowired
    private SourceStarvationService sourceStarvationService;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;
    private ClusterStampData currentClusterStamp;
    private long totalConfirmedTransactionsCount;


    @Override
    @PostConstruct
    public void init() {
        super.init();
        currentClusterStamp = new ClusterStampData();
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
            log.debug("CLUSTERSTAMP: Propagating ClusterStampPreparationData");
            totalConfirmedTransactionsCount = clusterStampPreparationData.getTotalConfirmedTransactionsCount();
            clusterStampState = clusterStampState.nextState(clusterStampPreparationData); //Change state to PREPARING
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
            CompletableFuture.runAsync(this::initPrepareForClusterStampTimer);
    }

    private void initPrepareForClusterStampTimer() {
        try {
            Thread.sleep(replyTimeOut);
            if(isPreparingForClusterStamp()) {
                log.info("Zero spend couldn't get a response from all DSP's for {} seconds. starting cluster stamp.", replyTimeOut);
                makeAndPropagateClusterStamp(new DspReadyForClusterStampData());
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();

        }
    }

    @Override
    //TODO 3/5/2019 astolia: decide if FullNodeReadyForClusterStamp List is required. if not - delete.
    // if yes - add validation for that list.
    public void getReadyForClusterStamp(ClusterStampStateData dspReadyForClusterStampData) {
        log.debug("CLUSTERSTAMP: Received DspReadyForClusterStampData");
        if(validationService.validateRequestAndPreparingState(dspReadyForClusterStampData, clusterStampState)){
            if(currentClusterStamp.getDspReadyForClusterStampDataList().contains(dspReadyForClusterStampData)) {
                log.warn("\'Dsp Node Ready For Cluster Stamp\' was already sent by the sender of this message");
                return;
            }
            currentClusterStamp.getDspReadyForClusterStampDataList().add((DspReadyForClusterStampData)dspReadyForClusterStampData);

            if ( currentClusterStamp.getDspReadyForClusterStampDataList().size() == NUMBER_OF_DSP_NODES ) {
                log.info("All DSP's are ready for cluster stamp. proceeding to create and propagate cluster stamp.");
                makeAndPropagateClusterStamp(dspReadyForClusterStampData);
            }
        }
    }

    /**
     * Creates and propagates cluster stamp.
     * Called after received 'dsp ready' from all DSP's OR after prepare for clusterstamp timer has expired.
     */
    private void makeAndPropagateClusterStamp(ClusterStampStateData dspReadyForClusterStampData) {
        synchronized (this) {
            if (isPreparingForClusterStamp()) {
                clusterStampState = clusterStampState.nextState((DspReadyForClusterStampData) dspReadyForClusterStampData); //Change state to READY
            } else {
                log.debug("Cluster stamp state is {}.  makeAndPropagateClusterStamp is already in process.", clusterStampState.name());
                return;
            }
        }
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
    }

    private void propagateClusterStampInProcessData() {
        //TODO 3/5/2019 astolia: why do we need list of DspReadyForClusterStamp? is it validated? can we improve it?
        ZeroSpendReadyForClusterStampData zerospendReadyForClusterStampData = new ZeroSpendReadyForClusterStampData(totalConfirmedTransactionsCount);
        zerospendReadyForClusterStampData.setDspReadyForClusterStampDataList(currentClusterStamp.getDspReadyForClusterStampDataList());

        clusterStampStateCrypto.signMessage(zerospendReadyForClusterStampData);
        propagationPublisher.propagate(zerospendReadyForClusterStampData, Arrays.asList(NodeType.DspNode));
        clusterStampState = clusterStampState.nextState(zerospendReadyForClusterStampData); //Change state to IN_PROCESS
    }

    /**
     * Handles DSP votes for whether the cluster stamp is correct or not.
     * TODO/NOTE/WARNING TBD- some cases aren't handled and may cause a bug. 1) if some DSP's votes are different.
     * TODO 2) if some of the DSP's didn't respond with a vote or their response didn't arrive.
     * @param dspClusterStampVoteData DSP vote regarding the cluster stamp.
     */
    public void handleDspClusterStampVote(DspClusterStampVoteData dspClusterStampVoteData) {

        log.debug("CLUSTERSTAMP: Received DspClusterStampVoteData");
        if(dspClusterStampVoteCrypto.verifySignature(dspClusterStampVoteData) && isClusterStampInProcess()) {
            ClusterStampData clusterStampData = clusterStamps.getByHash(dspClusterStampVoteData.getHash());

            List<DspClusterStampVoteData> dspClusterStampVotesRef = clusterStampData.getClusterStampConsensusResult().getDspClusterStampVoteDataList();
            if(dspClusterStampVotesRef.contains(dspClusterStampVoteData)){
                log.info("Received DspClusterStampVoteData is already found in the votes list");
                return;
            }

            dspClusterStampVotesRef.add(dspClusterStampVoteData);

            if(dspClusterStampVotesRef.size() == NUMBER_OF_DSP_NODES) {
                int validClusterStampVotes = countValidVotes(dspClusterStampVotesRef);
                if(validClusterStampVotes != NUMBER_OF_DSP_NODES){
                    log.error("Couldn't reach vote consensus.");
                    // TODO need to handle this case and return. as this voting should be ignored for now - not implemented yet.
                    // return; //uncomment after above to do is implemented
                }

                handleDspVotesResult(clusterStampData.getClusterStampConsensusResult());
                clusterStamps.put(clusterStampData);
                log.info("CLUSTERSTAMP: Clusterstamp is saved.");
            }
        }
    }

    /**
     * Goes over all the votes and counts the valid votes.
     * @param votes collection of votes from DSP's.
     * @return the number of valid votes.
     */
    private int countValidVotes(List<DspClusterStampVoteData> votes){
        int validClusterStampVotes = 0;
        for(DspClusterStampVoteData currentDspClusterStampVoteData : votes) {
            if( currentDspClusterStampVoteData.isValidClusterStamp) {
                validClusterStampVotes++;
            }
        }
        return validClusterStampVotes;
    }

    /**
     * Sends the result to relevant nodes.
     * restart services that were stopped for cluster stamp and changes cluster stamp state to off
     * @param result the result of cluster stamp votes from DSP's.
     */
    private void handleDspVotesResult(ClusterStampConsensusResult result){
        result.setDspConsensus(true);
        clusterStampConsensusResultCrypto.signMessage(result);
        propagationPublisher.propagate(result, Arrays.asList(NodeType.DspNode));

        dspVoteService.startSumAndSaveVotes();
        sourceStarvationService.startCheckSourcesStarvation();
        log.info("Restart DSP vote service to sum and save DSP votes, and starvation service");
        clusterStampState = clusterStampState.nextState(result); //Change state to OFF
    }

    @Override
    public  Set<Hash> getUnreachedDspcHashTransactions(){
        return dspVoteService.getTransactionHashToVotesListMapping().keySet();
    }
}