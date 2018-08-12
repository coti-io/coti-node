package io.coti.common.data.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;

public interface ISignValidatable{
    SignatureData getSignature();
    Hash getSignerHash();
}
