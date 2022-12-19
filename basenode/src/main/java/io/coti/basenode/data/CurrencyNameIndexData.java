package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.charset.StandardCharsets;

@Data
public class CurrencyNameIndexData implements IEntity {

    private Hash hash;
    private Hash currencyHash;

    public CurrencyNameIndexData(String currencyName, Hash currencyHash) {
        this.hash = CryptoHelper.cryptoHash(currencyName.toLowerCase().getBytes(StandardCharsets.UTF_8));
        this.currencyHash = currencyHash;
    }
}
