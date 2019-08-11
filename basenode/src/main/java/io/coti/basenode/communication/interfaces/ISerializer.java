package io.coti.basenode.communication.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;

public interface ISerializer {

    byte[] serialize(IPropagatable entity);

    String serializeAsString(IPropagatable entity);

    <T extends IPropagatable> T deserialize(byte[] bytes);

    <T extends IPropagatable> T deserialize(String string);

}
