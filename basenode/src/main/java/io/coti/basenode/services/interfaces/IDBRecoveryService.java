package io.coti.basenode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import org.rocksdb.BackupInfo;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IDBRecoveryService {

    void init();

    ResponseEntity<IResponse> getBackupBucket();

    ResponseEntity<IResponse> manualBackupDB();

    HashMap<String, HashMap<String, Long>> getBackUpLog();

    void clearBackupLog();

    boolean isBackup();

    AtomicBoolean getBackupInProgress();

    BackupInfo getLastBackupInfo();

    long getBackupStartedTime();

    long getEntireDuration();

    long getBackupDuration();

    long getUploadDuration();

    long getRemovalDuration();

    long getBackupSuccess();

    String getS3FolderName();
}
