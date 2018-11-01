package io.coti.nodemanager.model;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.model.Collection;
import io.coti.nodemanager.data.NodeHistoryData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class NodeHistory extends Collection<NodeHistoryData> {

    public NodeHistory(){}

    @PostConstruct
    public void init() {
        super.init();
    }
}
