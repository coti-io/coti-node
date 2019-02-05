package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.FullNodeReadyForClusterStampCrypto;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Handler for PrepareForSnapshot messages propagated to FullNode.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private boolean isClusterStampInProgress;

    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Autowired
    private FullNodeReadyForClusterStampCrypto fullNodeReadyForClusterStampCrypto;
    @Autowired
    private ISender sender;
    @PostConstruct
    private void init(){
        isClusterStampInProgress = false;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("\"prepare for cluster stamp\" propagated message received from DSP to FN");

        if(!isClusterStampInProgress) {
            FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData = new FullNodeReadyForClusterStampData(clusterStampPreparationData.getLastDspConfirmed());
            fullNodeReadyForClusterStampCrypto.signMessage(fullNodeReadyForClusterStampData);
            receivingServerAddresses.forEach(address -> sender.send(fullNodeReadyForClusterStampData, address));
            isClusterStampInProgress = true;
        }
        else {
            log.info("Full Node is already preparing for cluster stamp");
            //TODO 2/4/2019 astolia: Send to DSP that snapshot prepare is in process?
        }
    }

    @Override
    public boolean getIsClusterStampInProgress() {
        return isClusterStampInProgress;
    }
}