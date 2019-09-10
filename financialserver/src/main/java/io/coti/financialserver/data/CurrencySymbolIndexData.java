package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class CurrencySymbolIndexData implements IEntity {

    private Hash hash;
    private Hash currencyHash;

    public CurrencySymbolIndexData(String currencySymbol, Hash currencyHash) {
        this.hash = CryptoHelper.cryptoHash(currencySymbol.getBytes());
        this.currencyHash = currencyHash;
    }
}
