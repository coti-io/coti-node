package io.coti.basenode.data.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;

public interface ISignValidatable {
    SignatureData getSignature();

    Hash getSignerHash();
}
