package io.coti.fullnode.services;

import io.coti.basenode.data.SnapshotPreparationData;
import io.coti.basenode.services.BaseNodeSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnapshotService extends BaseNodeSnapshotService {

    @Override
    public void prepareForSnapshot(SnapshotPreparationData snapshotPreparationData) {
        boolean bp = true;
    }
}