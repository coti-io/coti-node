package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;

public interface ISerializer {

    byte[] serialize(IPropagatable entity);

    <T extends IPropagatable> T deserialize(byte[] bytes);

}
