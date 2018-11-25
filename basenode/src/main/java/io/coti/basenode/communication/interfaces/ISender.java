package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;

public interface ISender {

    void connectToNode(String receivingServerAddress);

    <T extends IEntity> void send(T toSend, String address);

    void disconnectFromNode(String receivingFullAddress, NodeType nodeType);
}
