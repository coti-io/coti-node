package io.coti.common.data.interfaces;

public interface ISignable{
    void signMessage();
    byte[] getMessageInBytes();
}
