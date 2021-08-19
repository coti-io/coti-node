package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Set;

@Data
public class UserCurrencyIndexData implements IEntity {

    private static final long serialVersionUID = -1267915811984926071L;
    private Hash userHash;
    private Set<Hash> tokenHashes;

    public UserCurrencyIndexData(Hash userHash, Set<Hash> tokenHashes) {
        this.userHash = userHash;
        this.tokenHashes = tokenHashes;
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
