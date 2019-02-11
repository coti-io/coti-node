package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import io.coti.basenode.data.DspReadyForClusterStampData;

public interface IClusterStampService {

    void prepareForClusterStamp(ClusterStampPreparationData prepareForSnapshot);

    void handleFullNodeReadyForClusterStampMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData);

    void dspNodeReadyForClusterStamp(DspReadyForClusterStampData clusterStampReadyData);

    void newClusterStamp(ClusterStampData clusterStampData);

    boolean isClusterStampInProgress();

}