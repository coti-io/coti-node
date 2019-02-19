package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampPreparationData;

public interface IClusterStampService {

    void init();

    void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData);

    boolean isClusterStampInProcess();

    boolean isClusterStampPreparing();

    boolean isClusterStampReady();

}
