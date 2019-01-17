package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.NodeType;

public interface ISender {

    void connectToNode(String receivingServerAddress);

    <T extends IPropagatable> void send(T toSend, String address);

    void disconnectFromNode(String receivingFullAddress, NodeType nodeType);
}
