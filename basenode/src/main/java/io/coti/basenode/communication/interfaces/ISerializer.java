package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IEntity;

public interface ISerializer {

    byte[] serialize(IEntity entity);

    <T extends IEntity> T deserialize(byte[] bytes);

}
