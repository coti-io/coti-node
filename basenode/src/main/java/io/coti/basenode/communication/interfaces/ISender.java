package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.io.IOException;
import java.util.Map;

public interface ISender {

    void connectToNode(String receivingServerAddress, NodeType nodeType);

    void initMonitor();

    <T extends IPropagatable> void send(T toSend, String address);

    void disconnectFromNode(String receivingFullAddress, NodeType nodeType);

    Map<String, NodeType> validateSenders() throws IOException;

    void shutdown();
}
