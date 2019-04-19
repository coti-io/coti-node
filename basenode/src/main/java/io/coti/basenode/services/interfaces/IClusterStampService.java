package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

import java.io.File;

/**
 * An interface that defines basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
public interface IClusterStampService {

    void loadBalanceFromLastClusterStamp();

    ClusterStampData getNewerClusterStamp(long totalConfirmedTransactionsPriorClusterStamp);

    SignatureData getNewerClusterStampSignature(long totalConfirmedTransactionsPriorClusterStamp);

    Hash getSignerHash(long totalConfirmedTransactionsPriorClusterStamp);

    File getNewerClusterStampFile();
}