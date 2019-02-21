package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.ClusterStampPreparationData;

public interface IClusterStampService {

    void init();

    void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData);

    boolean isPreparingForClusterStamp();

    boolean isReadyForClusterStamp();

    boolean isClusterStampInProcess();

    void loadBalanceFromClusterStamp(ClusterStampData clusterStampData);

    ClusterStampData getLastClusterStamp(long totalConfirmedTransactionsPriorClusterStamp);
}
