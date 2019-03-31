package io.coti.basenode.model;

import io.coti.basenode.data.NodeRegistrationData;
import org.springframework.stereotype.Service;

@Service
public class NodeRegistrations extends Collection<NodeRegistrationData> {

    public NodeRegistrations() {
    }

    public void init() {
        super.init();
    }

}
