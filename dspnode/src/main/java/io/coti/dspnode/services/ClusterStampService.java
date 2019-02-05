package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampPreparationCrypto;
import io.coti.basenode.crypto.FullNodeReadyForClusterStampCrypto;
import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.model.DspNodeReadyForClusterStamp;
import io.coti.basenode.data.DspNodeReadyForClusterStampData;
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

    private boolean isClusterStampInProgress;
    private boolean isReadyForClusterStamp;
    private int fullNodesMajority;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStampPreparationCrypto clusterStampPreparationCrypto;
    @Autowired
    private FullNodeReadyForClusterStampCrypto fullNodeReadyForClusterStampCrypto;
    @Autowired
    private DspNodeReadyForClusterStamp dspNodeReadyForClusterStamp;

    @PostConstruct
    private void init() {
        isClusterStampInProgress = false;
        isReadyForClusterStamp = false;
        fullNodesMajority = 2;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("\"prepare for cluster stamp\" propagated message received from ZS to DSP");
        if( !isClusterStampInProgress && clusterStampPreparationCrypto.verifySignature(clusterStampPreparationData)) {
            isClusterStampInProgress = true;

            clusterStampPreparationCrypto.signMessage(clusterStampPreparationData);
            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.FullNode));
        }
        else {
            log.info("DSP node is already preparing for cluster stamp");
            //TODO 2/4/2019 astolia: Send to ZS that snapshot prepare is in process?
        }
    }

    public void fullNodeReadyForClusterStamp(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {

        log.debug("\"ready for cluster stamp\" propagated message received from FN to DSP");
        if(isClusterStampInProgress && !isReadyForClusterStamp && fullNodeReadyForClusterStampCrypto.verifySignature(fullNodeReadyForClusterStampData)) {
            DspNodeReadyForClusterStampData dspNodeReadyForClusterStampData = dspNodeReadyForClusterStamp.getByHash(fullNodeReadyForClusterStampData.getHash());

            if ( dspNodeReadyForClusterStampData == null ) {
                dspNodeReadyForClusterStampData = new DspNodeReadyForClusterStampData(fullNodeReadyForClusterStampData.getHash(), fullNodeReadyForClusterStampData);
            }

            else {
                dspNodeReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().add(fullNodeReadyForClusterStampData);
            }

            if ( dspNodeReadyForClusterStampData.getFullNodeReadyForClusterStampDataList().size() >= fullNodesMajority ) {
                isReadyForClusterStamp = true;
                propagationPublisher.propagate(dspNodeReadyForClusterStampData, Arrays.asList(NodeType.DspNode, NodeType.ZeroSpendServer));
            }

            dspNodeReadyForClusterStamp.put(dspNodeReadyForClusterStampData);
        }
    }

    @Override
    public boolean getIsClusterStampInProgress() {
        return isClusterStampInProgress;
    }

}