package io.coti.cotinode.service;

import io.coti.cotinode.data.NodeInformation;
import io.coti.cotinode.http.interfaces.NodeInformationResponse;
import io.coti.cotinode.service.interfaces.INodeInformationService;
import org.springframework.stereotype.Service;

@Service
public class NodeInformationService implements INodeInformationService {

    public NodeInformationResponse getNodeInformation(){
        return new NodeInformationResponse();
    }
}
