package io.coti.financialserver.http.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CurrencyDataForFee implements Serializable {

    @NotEmpty
    protected String name;
    @NotEmpty
    protected String symbol;
    @Positive
    protected BigDecimal totalSupply;

    protected CurrencyDataForFee() {
    }

    public CurrencyDataForFee(CurrencyDataForFee originatorCurrencyData) {
        name = originatorCurrencyData.getName();
        symbol = originatorCurrencyData.getSymbol();
        totalSupply = originatorCurrencyData.getTotalSupply();
    }

    public Hash calculateHash() {
        return CryptoHelper.cryptoHash(symbol.getBytes(), 224);
    }
}
