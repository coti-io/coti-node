package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import io.coti.basenode.data.DspNodeReadyForClusterStampData;

public interface IClusterStampService {

    void prepareForClusterStamp(ClusterStampPreparationData prepareForSnapshot);

    void fullNodeReadyForClusterStamp(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData);

    void dspNodeReadyForClusterStamp(DspNodeReadyForClusterStampData clusterStampReadyData);

    void newClusterStamp(ClusterStampData clusterStampData);
}