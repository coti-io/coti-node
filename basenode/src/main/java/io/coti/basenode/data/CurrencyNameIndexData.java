package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class CurrencyNameIndexData implements IEntity {
    Hash hash; // Currency Name Hash
    Hash currencyHash;

    public CurrencyNameIndexData(Hash hash, Hash currencyHash) {
        this.hash = hash;
        this.currencyHash = currencyHash;
    }
}
