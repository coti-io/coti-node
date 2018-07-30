package io.coti.common.communication.interfaces;

import io.coti.common.data.interfaces.IEntity;

public interface ISerializer {

    byte[] serialize(IEntity entity);

    <T extends IEntity> T deserialize(byte[] bytes);

}
