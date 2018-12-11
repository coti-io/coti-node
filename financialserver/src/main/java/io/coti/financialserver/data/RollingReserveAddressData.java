package io.coti.financialserver.data;

import lombok.Data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

@Data
public class RollingReserveAddressData implements IEntity {
    Hash merchantHash;
    Hash rollingReserveAddress;

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }
}
