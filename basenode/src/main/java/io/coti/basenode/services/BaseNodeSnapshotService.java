package io.coti.basenode.services;

import io.coti.basenode.data.PrepareForSnapshot;
import io.coti.basenode.services.interfaces.ISnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeSnapshotService implements ISnapshotService {

    @Override
    public void handlePrepareForSnapshot(PrepareForSnapshot prepareForSnapshot) {
        boolean bp = true;
    }
}
