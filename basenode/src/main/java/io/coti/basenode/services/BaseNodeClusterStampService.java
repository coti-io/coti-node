package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData prepareForSnapshot) {}

    @Override
    public void handleFullNodeReadyForClusterStampMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {
    }

    @Override
    public void handleDspNodeReadyForClusterStampMessage(DspReadyForClusterStampData clusterStampReadyData) {
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {
    }

    @Override
    public boolean isClusterStampInProgress() {
        return false;
    }

}
