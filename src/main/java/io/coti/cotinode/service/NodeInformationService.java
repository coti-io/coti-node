package io.coti.cotinode.service;

import io.coti.cotinode.data.NodeInformation;
import org.springframework.stereotype.Service;

@Service
public class NodeInformationService {

    public NodeInformation getNodeInformation(){
        return new NodeInformation("Node 1", "10.0.0.1");
    }
}
