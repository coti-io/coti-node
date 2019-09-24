package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;

@Data
public class UserTokenGenerationData implements IEntity {

    private Hash userHash;
    private Map<Hash, Hash> transactionHashToCurrencyMap;

    public UserTokenGenerationData(Hash userHash, Map<Hash, Hash> transactionHashToCurrencyMap) {
        this.userHash = userHash;
        this.transactionHashToCurrencyMap = transactionHashToCurrencyMap;
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
