package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.SnapshotPreparationCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SnapshotPreparationData;
import io.coti.basenode.services.interfaces.ISnapshotService;
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
public class SnapshotService implements ISnapshotService {

    private boolean isSnapshotInProgress;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private SnapshotPreparationCrypto snapshotPreparationCrypto;

    @PostConstruct
    private void init(){
        isSnapshotInProgress = false;
    }

    @Override
    public void prepareForSnapshot(SnapshotPreparationData snapshotPreparationDataZs) {
        log.debug("\"prepare for snapshot\" propagated message received from ZS to DSP");
        if(!isSnapshotInProgress) {
            isSnapshotInProgress = true;

            SnapshotPreparationData prepareForSnapshotDsp = new SnapshotPreparationData(snapshotPreparationDataZs.getLastDspConfirmed());
            snapshotPreparationCrypto.signMessage(prepareForSnapshotDsp);

            propagationPublisher.propagate(prepareForSnapshotDsp, Arrays.asList(NodeType.FullNode));
        }
        else{
            log.info("DSP node is already preparing for snapshot");
            //TODO 2/4/2019 astolia: Send to ZS that snapshot prepare is in process?
        }
    }

    @Override
    public boolean getIsSnapshotInProgress() {
        return isSnapshotInProgress;
    }

}