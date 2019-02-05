package io.coti.basenode.model;

import io.coti.basenode.data.NodeRegistrationData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
@Service
public class NodeRegistrations extends Collection<NodeRegistrationData> {

    public NodeRegistrations(){}

    @PostConstruct
    public void init() {
        super.init();
    }

}
