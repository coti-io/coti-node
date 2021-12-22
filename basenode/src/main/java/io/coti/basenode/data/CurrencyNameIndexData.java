package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class CurrencyNameIndexData implements IEntity {

    private Hash hash;
    private Hash currencyHash;

    public CurrencyNameIndexData(String currencyName, Hash currencyHash) {
        this.hash = CryptoHelper.cryptoHash(currencyName.toLowerCase().getBytes());
        this.currencyHash = currencyHash;
    }
}
