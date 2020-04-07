package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.HashSet;

@Data
public class UserCurrencyIndexData implements IEntity {

    private static final long serialVersionUID = -1267915811984926071L;
    private Hash userHash;
    private HashSet<Hash> tokens;

    public UserCurrencyIndexData(Hash userHash, HashSet<Hash> tokens) {
        this.userHash = userHash;
        this.tokens = tokens;
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash userHash) {
        this.userHash = userHash;
    }

}
