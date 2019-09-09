package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AddressUserIndexData implements IEntity {

    private static final long serialVersionUID = 8391930836097202144L;
    private Hash address;
    private Hash userHash;
    private boolean zeroTrustFlag;

    public AddressUserIndexData(Hash address, Hash userHash) {
        this.address = address;
        this.userHash = userHash;
        this.zeroTrustFlag = false;
    }

    @Override
    public Hash getHash() {
        return address;
    }

    @Override
    public void setHash(Hash hash) {
        this.address = hash;
    }

}