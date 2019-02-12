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

    final private static int NUMBER_OF_FULL_NODES = 3;

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

    @Value("${zerospend.receiving.address}")
    private String receivingZerospendAddress;

    @Value("${clusterstamp.reply.timeout}")

    private int replyTimeOut;

    private boolean clusterStampInProgress;

    private int readyForClusterStampMsgCount;

    @PostConstruct
    private void init() {
        clusterStampInProgress = false;
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
            clusterStampInProgress = true;
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
        if(!clusterStampInProgress && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)) {

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
                clusterStampInProgress = true;
                clusterStampStateCrypto.signMessage(dspReadyForClusterStampData);
                sender.send(dspReadyForClusterStampData, receivingZerospendAddress);
                dspReadyForClusterStampMessages.deleteByHash(fullNodeReadyForClusterStampData.getHash());
            }
        }
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            clusterStampInProgress = false;

        }
    }

    @Override
    public boolean isClusterStampInProgress() {
        return clusterStampInProgress;
    }
}