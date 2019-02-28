package io.coti.basenode.http.interfaces;

public interface ISerializer {
    byte[] serialize(ISerializable entity);

    <T extends ISerializable> T deserialize(byte[] bytes);
}
