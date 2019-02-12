package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
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

    final private static int FULL_NODES_MAJORITY = 1;

    final private static int NUMBER_OF_FULL_NODES = 3;

    private boolean clusterStampInProgress;

    private boolean readyForClusterStamp;

    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;

    @Value("${clusterstamp.reply.timeout}")
    private int replyTimeOut;

    private int readyForClusterStampMsgCount;

    private boolean majorityMode;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private DspReadyForClusterStamp dspReadyForClusterStampMessages;
    @Autowired
    private ISender sender;

    @PostConstruct
    private void init() {
        clusterStampInProgress = false;
        readyForClusterStamp = false;
        majorityMode = false;
        readyForClusterStampMsgCount = 0;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("Prepare for cluster stamp propagated message received from ZS to DSP");
        if(validatePrepareForClusterStampRequest(clusterStampPreparationData)){
            clusterStampInProgress = true;
            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
            CompletableFuture.runAsync(() -> initMajorityTimer());
        }
    }

    private void initMajorityTimer(){
        try {
            Thread.sleep(replyTimeOut);
            majorityMode = true;
            log.info("DSP node didn't receive responses from all full nodes. Starting majority timer");
            Thread.sleep(replyTimeOut);
            if(!readyForClusterStamp){
                clusterStampInProgress = false;
                log.error("ClusterStamp failed! DSP node didn't receive responses from majority of full nodes.");
                //TODO 2/11/2019 astolia: clean up stuff
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private boolean validatePrepareForClusterStampRequest(ClusterStampPreparationData clusterStampPreparationData){
        if(clusterStampInProgress){
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
        if(clusterStampInProgress && !readyForClusterStamp && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)) {

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
                readyForClusterStamp = true;
                //TODO 2/10/2019 astolia: start cluster stamp:
                // clear all received message. need to save them for some reason?
                // Starts rejecting new transactions from Full Node’s
                // Sends DspReadyForClusterStampData to ZS
                return;
            }

            if ( majorityMode && dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().size() >= FULL_NODES_MAJORITY ) {
                readyForClusterStamp = true;
                clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
                sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
            }
        }
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            readyForClusterStamp = false;

        }
    }

    @Override
    public boolean isReadyForClusterStamp() {
        return readyForClusterStamp;
    }

}