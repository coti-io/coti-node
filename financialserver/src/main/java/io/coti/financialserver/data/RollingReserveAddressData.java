package io.coti.financialserver.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

@Data
public class RollingReserveAddressData implements IEntity, ISignable, ISignValidatable {

    Hash merchantHash;
    Hash rollingReserveAddress;
    Integer addressIndex;
    private SignatureData merchantSignature;

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return merchantSignature;
    }

    @Override
    public Hash getSignerHash() {
        return merchantHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        merchantHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        merchantSignature = signature;
    }
}
