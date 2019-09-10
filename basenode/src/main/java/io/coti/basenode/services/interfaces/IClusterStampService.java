package io.coti.basenode.services.interfaces;


import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

/**
 * An interface that defines basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
public interface IClusterStampService {

    void init();

    ResponseEntity<IResponse> getRequiredClusterStampNames();

    void getClusterStampFromRecoveryServer(boolean isStartup);

    void generateNativeTokenClusterStamp();

}