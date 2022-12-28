package io.coti.basenode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import org.rocksdb.BackupInfo;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IDBRecoveryService {

    void init();

    ResponseEntity<IResponse> getBackupBucket();

    ResponseEntity<IResponse> manualBackupDB();

    void clearBackupLog();

    boolean isBackup();

    AtomicBoolean getBackupInProgress();

    BackupInfo getLastBackupInfo();

    long getBackupStartedTime();

    long getEntireDuration();

    long getBackupDuration();

    long getUploadDuration();

    long getRemovalDuration();

}
