package io.coti.basenode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IDBRecoveryService {

    void init();

    ResponseEntity<IResponse> getBackupBucket();
}
