package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspReadyForClusterStamp;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A service that provides Cluster Stamp functionality for DSP node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

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
    private DspClusterStampVoteCrypto dspClusterStampVoteCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;
    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;
    private int readyForClusterStampMsgCount;
    private boolean zeroSpendReady;


    @Override
    @PostConstruct
    public void init() {
        super.init();
        readyForClusterStampMsgCount = 0;
        zeroSpendReady = false;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        log.info("CLUSTERSTAMP: Received ClusterStampPreparationData");
        if(validationService.validateRequestAndOffState(clusterStampPreparationData, clusterStampState)){
            log.debug("Start preparation of DSP for cluster stamp");
            clusterStampState = clusterStampState.nextState(clusterStampPreparationData); //Change state to PREPARING

            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
            CompletableFuture.runAsync(this::initReadyForClusterStampTimer);
        }
    }

    private void initReadyForClusterStampTimer(){
        try {
            Thread.sleep(replyTimeOut);
            if(isPreparingForClusterStamp()) {
                log.info("DSP couldn't get a response from all Full nodes for {} seconds. proceeding without all responses.", replyTimeOut);
                sendDspReadyForClusterStamp(new DspReadyForClusterStampData(),false);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void getReadyForClusterStamp(ClusterStampStateData fullNodeReadyForClusterStampData){

        log.info("CLUSTERSTAMP: Received FullNodeReadyForClusterStampData");
        if(validationService.validateRequestAndPreparingState(fullNodeReadyForClusterStampData,clusterStampState)) {
            DspReadyForClusterStampData dspReadyForClusterStampData = dspReadyForClusterStamp.getByHash(fullNodeReadyForClusterStampData.getHash());

            if (dspReadyForClusterStampData == null) {
                dspReadyForClusterStampData = new DspReadyForClusterStampData(fullNodeReadyForClusterStampData.getTotalConfirmedTransactionsCount());
            }
            else if(dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().contains(fullNodeReadyForClusterStampData) && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)){
                log.warn("\'Full Node Ready For Cluster Stamp\' was already sent by the sender of this message");
                return;
            }

            dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().add((FullNodeReadyForClusterStampData)fullNodeReadyForClusterStampData);
            dspReadyForClusterStamp.put(dspReadyForClusterStampData);
            readyForClusterStampMsgCount++;

            if(NUMBER_OF_FULL_NODES == readyForClusterStampMsgCount) {
                log.info("All full nodes are ready for cluster stamp");
                sendDspReadyForClusterStamp(dspReadyForClusterStampData,true);
            }
        }
    }

    /**
     * Changes to next cluster stamp state and notifies Full nodes and Zero spend node that this DSP node is ready for cluster stamp.
     * Called after received 'full node ready' from all Full Nodes OR after prepare for clusterstamp timer has expired.
     */
    private void sendDspReadyForClusterStamp(DspReadyForClusterStampData dspReadyForClusterStampData, boolean majorityReached) {
        synchronized (this) {
            if (isPreparingForClusterStamp()) {
                clusterStampState = clusterStampState.nextState(dspReadyForClusterStampData); //Change state to READY
            } else {
                log.debug("Cluster stamp state is {}.  sendDspReadyForClusterStamp is already in process.", clusterStampState.name());
                return;
            }
        }
        if(!majorityReached){
            dspReadyForClusterStamp.deleteByHash(dspReadyForClusterStampData.getHash());
        }
        sendDspReadyForClusterStamp(dspReadyForClusterStampData);
    }

    //TODO 3/6/2019 astolia: Check if need to handle concurrency here.
    /**
     * Notifies Full nodes and Zero spend node that this DSP node is ready for cluster stamp.
     * @param dspReadyForClusterStampData
     */
    private void sendDspReadyForClusterStamp(DspReadyForClusterStampData dspReadyForClusterStampData){
        log.info("Sending DspReadyForClusterStampData");
        clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
        //TODO 3/3/2019 astolia: need to propagate also to other dsps?
        // TODO if so - need to add handler for DspReadyForClusterStampData messages in DSP ClusterStampService.
        propagationPublisher.propagate(dspReadyForClusterStampData, Arrays.asList(NodeType.FullNode));
        sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
        readyForClusterStampMsgCount = 0;
        if(zeroSpendReady){
            clusterStampState = clusterStampState.nextState(dspReadyForClusterStampData); //Change state to IN_PROCESS
            zeroSpendReady = false;
        }
    }

    public void voteAndStoreClusterStamp(ClusterStampData clusterStampData) {
        log.info("Received ClusterStampData for vote and store.");
        if(clusterStampCrypto.verifySignature(clusterStampData) && isClusterStampInProcess()) {
            ClusterStampData clusterStampDataLocal = new ClusterStampData();
            clusterStampDataLocal.setBalanceMap(balanceService.getBalanceMap());
            clusterStampDataLocal.setUnconfirmedTransactions(getUnconfirmedTransactions());
            setHash(clusterStampDataLocal);

            clusterStampCrypto.signMessage(clusterStampDataLocal);
            propagationPublisher.propagate(clusterStampDataLocal, Arrays.asList(NodeType.FullNode));

            boolean validClusterStamp = clusterStampData.getHash().equals(clusterStampDataLocal.getHash());
            DspClusterStampVoteData dspClusterStampVoteData = new DspClusterStampVoteData(clusterStampData.getHash(), validClusterStamp);

            dspClusterStampVoteCrypto.signMessage(dspClusterStampVoteData);
            log.info("Sending DspClusterStampVoteData");
            sender.send(dspClusterStampVoteData, receivingZerospendAddress);

            if(validClusterStamp) {
                clusterStamps.put(clusterStampData);
                log.info("CLUSTERSTAMP: saved cluster stamp without consensus");
            }
        }
    }


    //TODO 3/3/2019 astolia: need to validate the DSP DspReadyForClusterStampData list in zerospendReadyForClusterStampData.
    public void handleZeroSpendReadyForClusterStampData(ZeroSpendReadyForClusterStampData zerospendReadyForClusterStampData){
        if(!clusterStampStateCrypto.verifySignature(zerospendReadyForClusterStampData)){
            log.error("Wrong signature for \'prepare for cluster stamp\' request.");
            //TODO 3/5/2019 astolia: exit cluster stamp flow? need to clean up somehow?
            return;
        }

        synchronized (this) {

            // State should be READY in case of normal flow
            // (Should Get responses from all FN before ZeroSpendReadyForClusterStampData)
            if (isReadyForClusterStamp()) {
                    clusterStampState = clusterStampState.nextState(zerospendReadyForClusterStampData); //Change state to IN_PROCESS
                    log.debug("DSP is waiting for Cluster stamp from Zero spend");
                }

            // State will be IN_PROCESS In case ZeroSpendReadyForClusterStampData received before all
            // fullNodeReadyForClusterStampData responses received.
            else if (isPreparingForClusterStamp()) {
                zeroSpendReady = true;
            }
        }
    }

    @Override
    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
        if(isClusterStampInProcess()) {
            log.info("CLUSTERSTAMP: Received ClusterStampConsensusResult");
            super.handleClusterStampConsensusResult(clusterStampConsensusResult);
            propagationPublisher.propagate(clusterStampConsensusResult, Arrays.asList(NodeType.FullNode));
            clusterStampState = clusterStampState.nextState(clusterStampConsensusResult); //Change state to OFF
        }
    }

    @Override
    public Set<Hash> getUnreachedDspcHashTransactions(){
        return dspVoteService.getTransactionHashToVotesListMapping().keySet();
    }
}