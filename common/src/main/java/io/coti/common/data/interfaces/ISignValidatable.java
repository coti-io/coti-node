package io.coti.common.data.interfaces;

public interface ISignValidatable{
    boolean verifySignature();
    byte[] getMessageInBytes();
}
