package io.coti.basenode.services;

import io.coti.basenode.http.NodeInformationResponse;
import io.coti.basenode.services.interfaces.INodeInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@Slf4j
public class NodeInformationService implements INodeInformationService {

    public NodeInformationResponse getNodeInformation() {
        NodeInformationResponse response = new NodeInformationResponse();
        try {
            response.setIpV4Address(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log.error("Unknown host at node information", e);
        }
        return response;
    }
}
