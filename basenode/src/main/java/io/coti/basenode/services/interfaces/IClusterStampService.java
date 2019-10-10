package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.InitiatedTokenNoticeData;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IClusterStampService {

    void init();

    boolean shouldUpdateClusterStampDBVersion();

    boolean isClusterStampDBVersionExist();

    void setClusterStampDBVersion();

    ResponseEntity<IResponse> getRequiredClusterStampNames();

    void getClusterStampFromRecoveryServer(boolean isStartup);

    void handleInitiatedTokenNotice(InitiatedTokenNoticeData initiatedTokenNoticeData);

}