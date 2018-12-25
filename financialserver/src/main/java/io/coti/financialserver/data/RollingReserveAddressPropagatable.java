package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class RollingReserveAddressPropagatable implements IPropagatable {

    Hash merchantHash;
    Hash addressIndex;

    public RollingReserveAddressPropagatable(Hash merchantHash, Hash addressIndex) {
        this.merchantHash = merchantHash;
        this.addressIndex = addressIndex;
    }

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        merchantHash = hash;
    }
}
