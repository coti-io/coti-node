package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

public interface IClusterStampService {

    void init();

    boolean isClusterStampOff();

    boolean isPreparingForClusterStamp();

    boolean isReadyForClusterStamp();

    boolean isClusterStampInProcess();

    void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData);

    void getReadyForClusterStamp(ClusterStampStateData nodeReadyForClusterStampData);

    void loadBalanceFromClusterStamp(ClusterStampData clusterStampData);

    ClusterStampData getLastClusterStamp(long totalConfirmedTransactionsPriorClusterStamp);

}
