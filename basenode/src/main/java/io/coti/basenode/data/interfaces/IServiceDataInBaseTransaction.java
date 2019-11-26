package io.coti.basenode.data.interfaces;

import java.io.Serializable;

public interface IServiceDataInBaseTransaction extends Serializable {
    byte[] getMessageInBytes();
}
