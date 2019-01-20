package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class RollingReserveMerchantAddressRequest implements ISignable, ISignValidatable {

    @NotNull
    Hash trustScoreHash;
    @NotNull
    Hash merchantHash;
    @NotNull
    private SignatureData signature;

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return trustScoreHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        trustScoreHash = signerHash;
    }
}
