package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

/**
 * An interface that defines basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
public interface IClusterStampService {

    /**
     * Initiates cluster stamp service.
     */
    void init();

    /**
     * returns whether the current cluster stamp is OFF.
     * @return true if the current cluster stamp is OFF. false otherwise.
     */
    boolean isClusterStampOff();

    /**
     * returns whether the current cluster stamp is PREPARING.
     * @return true if the current cluster stamp is PREPARING. false otherwise.
     */
    boolean isPreparingForClusterStamp();

    /**
     * returns whether the current cluster stamp is READY.
     * @return true if the current cluster stamp is READY. false otherwise.
     */
    boolean isReadyForClusterStamp();

    /**
     * returns whether the current cluster stamp is IN_PROCESS.
     * @return true if the current cluster stamp is IN_PROCESS. false otherwise.
     */
    boolean isClusterStampInProcess();

    /**
     * Changes the nodes state to PREPARING
     * and notifies other nodes that current the node has started the Cluster Stamp flow
     * by sending or propagating the relevant message.
     * @param clusterStampPreparationData the message sent or propagated to the handling node.
     *                                   Letting it know that it should start the cluster stamp flow.
     */
    void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData);

    /**
     * Handles required steps for transitioning of the cluster stamp state of the current node to READY.
     * @param nodeReadyForClusterStampData the required data for transitioning to READY state.
     */
    void getReadyForClusterStamp(ClusterStampStateData nodeReadyForClusterStampData);

    void loadBalanceFromClusterStamp(ClusterStampData clusterStampData);

    ClusterStampData getLastClusterStamp(long totalConfirmedTransactionsPriorClusterStamp);

}
