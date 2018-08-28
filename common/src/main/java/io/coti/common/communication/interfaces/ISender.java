package io.coti.common.communication.interfaces;

import io.coti.common.data.interfaces.IEntity;

import java.util.List;

public interface ISender {

    void init(List<String> receivingServerAddresses);

    <T extends IEntity> void send(T toSend, String address);
}
