package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IEntity;

import java.util.List;

public interface ISender {

    void init(List<String> receivingServerAddresses);

    <T extends IEntity> void send(T toSend, String address);
}
