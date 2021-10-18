package io.coti.basenode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface IDBRecoveryService {

    void init();

    ResponseEntity<IResponse> getBackupBucket();

    ResponseEntity<IResponse> manualBackupDB();

    HashMap<String, HashMap<String, Long>> getBackUpLog();

    void clearBackupLog();
}
