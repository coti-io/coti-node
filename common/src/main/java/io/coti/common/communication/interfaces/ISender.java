package io.coti.common.communication.interfaces;

import io.coti.common.data.interfaces.IEntity;

public interface ISender {

    <T extends IEntity> void send(T toSend, String address);
}
