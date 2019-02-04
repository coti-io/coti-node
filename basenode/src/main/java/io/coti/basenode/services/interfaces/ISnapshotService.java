package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.SnapshotPreparationData;

public interface ISnapshotService {

    void prepareForSnapshot(SnapshotPreparationData prepareForSnapshot);

    boolean getIsSnapshotInProgress();
}