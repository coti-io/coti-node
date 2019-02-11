package io.coti.basenode.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.DspReadyForClusterStampData;
import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData prepareForSnapshot) {}

    @Override
    public void handleFullNodeReadyForClusterStampMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {
    }

    @Override
    public void dspNodeReadyForClusterStamp(DspReadyForClusterStampData clusterStampReadyData) {
    }

    @Override
    public void newClusterStamp(ClusterStampData clusterStampData) {
    }

    @Override
    public boolean isClusterStampInProgress() {
        return false;
    }
}
