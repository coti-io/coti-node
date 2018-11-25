package io.coti.nodemanager.model;

import io.coti.basenode.model.Collection;
import io.coti.nodemanager.data.ActiveNodeData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ActiveNode extends Collection<ActiveNodeData> {

    private ActiveNode() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
