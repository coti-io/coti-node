package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamps;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for PrepareForSnapshot messages propagated to FullNode.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    private ISender sender;
    @Autowired
    private ClusterStamps clusterStamps;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @PostConstruct
    private void init(){
        isReadyForClusterStamp = false;
    }

    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("Prepare for cluster stamp propagated message received from DSP to FN");

        if(!isReadyForClusterStamp && clusterStampStateCrypto.verifySignature(clusterStampPreparationData)) {
            FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData = new FullNodeReadyForClusterStampData(clusterStampPreparationData.getLastDspConfirmed());
            clusterStampStateCrypto.signMessage(fullNodeReadyForClusterStampData);
            receivingServerAddresses.forEach(address -> sender.send(fullNodeReadyForClusterStampData, address));
            isReadyForClusterStamp = true;
        }
        else {
            log.info("Full Node is already preparing for cluster stamp");
            //TODO 2/4/2019 astolia: Send to DSP that snapshot prepare is in process?
        }
    }

    public void newClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            clusterStamp.put(clusterStampData);
        }
    }
}