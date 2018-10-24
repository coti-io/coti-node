package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;

public interface ISender {

    void addAddress(String receivingServerAddress);

    <T extends IEntity> void send(T toSend, String address);

    void removeAddress(String receivingFullAddress, NodeType nodeType);
}
