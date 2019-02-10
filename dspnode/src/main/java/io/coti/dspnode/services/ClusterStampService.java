package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspNodeReadyForClusterStamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Handler for PrepareForSnapshot messages propagated to DSP.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int FULL_NODES_MAJORITY = 1;

    private boolean isClusterStampInProgress;
    private boolean isReadyForClusterStamp;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private DspNodeReadyForClusterStamp dspNodeReadyForClusterStamp;
    @Autowired
    private ClusterStamp clusterStamp;

    @PostConstruct
    private void init() {
        isReadyForClusterStamp = false;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("Prepare for cluster stamp propagated message received from ZS to DSP");
        if( !isClusterStampInProgress && clusterStampStateCrypto.verifySignature(clusterStampPreparationData)) {
            isClusterStampInProgress = true;

            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
        }
        else {
            log.info("DSP node is already preparing for cluster stamp");
            //TODO 2/4/2019 astolia: Send to ZS that snapshot prepare is in process?
        }
    }

    public void fullNodeReadyForClusterStamp(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {

        log.debug("Ready for cluster stamp propagated message received from FN to DSP");
        if(isClusterStampInProgress && !isReadyForClusterStamp && clusterStampStateCrypto.verifySignature(fullNodeReadyForClusterStampData)) {
            DspReadyForClusterStampData dspReadyForClusterStampData = dspNodeReadyForClusterStamp.getByHash(fullNodeReadyForClusterStampData.getHash());

            if ( dspReadyForClusterStampData == null ) {
                dspReadyForClusterStampData = new DspReadyForClusterStampData(fullNodeReadyForClusterStampData.getLastDspConfirmed());
            }

            dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().add(fullNodeReadyForClusterStampData);

            if ( dspReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().size() >= FULL_NODES_MAJORITY ) {
                isReadyForClusterStamp = true;
                propagationPublisher.propagate(dspReadyForClusterStampData, Arrays.asList(NodeType.DspNode, NodeType.ZeroSpendServer));
            }

            dspNodeReadyForClusterStamp.put(dspReadyForClusterStampData);
        }
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {

        boolean bp = true;
        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            isReadyForClusterStamp = false;



        }
    }

    public boolean getIsReadyForClusterStamp() {
        return isReadyForClusterStamp;
    }

}