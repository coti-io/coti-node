package io.coti.fullnode.services;

import io.coti.basenode.data.SnapshotPreparationData;
import io.coti.basenode.services.interfaces.ISnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Handler for PrepareForSnapshot messages propagated to FullNode.
 */
@Slf4j
@Service
public class SnapshotService  implements ISnapshotService {

    private boolean isSnapshotInProgress;

    @PostConstruct
    private void init(){
        isSnapshotInProgress = false;
    }

    @Override
    public void prepareForSnapshot(SnapshotPreparationData snapshotPreparationDataDSP) {
        log.debug("\"prepare for snapshot\" propagated message received from DSP to FN");
        if(!isSnapshotInProgress){
            isSnapshotInProgress = true;
        }
        else{
            log.info("Full Node is already preparing for snapshot");
            //TODO 2/4/2019 astolia: Send to DSP that snapshot prepare is in process?
        }
    }

    @Override
    public boolean getIsSnapshotInProgress() {
        return isSnapshotInProgress;
    }
}