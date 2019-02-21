package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class GetMerchantRollingReserveAddressRequest implements ISignable, ISignValidatable {

    @NotNull
    Hash trustScoreNodeHash;
    @NotNull
    Hash merchantHash;
    @NotNull
    private SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return trustScoreNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        trustScoreNodeHash = signerHash;
    }
}
