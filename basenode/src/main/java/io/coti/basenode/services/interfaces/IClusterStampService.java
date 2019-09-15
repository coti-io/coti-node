package io.coti.basenode.services.interfaces;


import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IClusterStampService {

    void init();

    ResponseEntity<IResponse> getRequiredClusterStampNames();

    void getClusterStampFromRecoveryServer(boolean isStartup);

}