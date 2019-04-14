package io.coti.nodemanager.model;

import io.coti.basenode.model.Collection;
import io.coti.nodemanager.data.ActiveNodeData;
import org.springframework.stereotype.Service;

@Service
public class ActiveNodes extends Collection<ActiveNodeData> {

    public ActiveNodes() {
    }

    public void init() {
        super.init();
    }
}
