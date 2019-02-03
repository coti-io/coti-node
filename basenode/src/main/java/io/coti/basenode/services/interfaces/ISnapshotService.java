package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.PrepareForSnapshot;

public interface ISnapshotService {

    void handlePrepareForSnapshot(PrepareForSnapshot prepareForSnapshot);
}