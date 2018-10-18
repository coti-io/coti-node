package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IEntity;

import java.util.List;

public interface ISender {

    void addAddress(String receivingServerAddress, String receivingServerPort);

    <T extends IEntity> void send(T toSend, String address);
}
