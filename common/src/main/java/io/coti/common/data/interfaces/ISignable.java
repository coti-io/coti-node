package io.coti.common.data.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;

public interface ISignable{
     void setSignerHash(Hash signerHash);
     void setSignature(SignatureData signature);
}
