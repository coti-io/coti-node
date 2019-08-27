package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class CurrencySymbolIndexData implements IEntity {

    Hash hash; // Currency Symbol Hash
    Hash currencyHash;

    public CurrencySymbolIndexData(Hash hash, Hash currencyHash) {
        this.hash = hash;
        this.currencyHash = currencyHash;
    }
}
