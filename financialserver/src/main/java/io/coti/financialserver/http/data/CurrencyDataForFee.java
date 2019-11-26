package io.coti.financialserver.http.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CurrencyDataForFee {

    @NotEmpty
    protected String name;
    @NotEmpty
    protected String symbol;
    @Positive
    protected BigDecimal totalSupply;
    @Range(min = 0, max = 12)
    protected int scale;

    public Hash calculateHash() {
        return CryptoHelper.cryptoHash(symbol.getBytes(), 224);
    }
}
