package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OriginatorCurrencyData implements Serializable {

    @NotEmpty
    protected String name;
    @NotEmpty
    protected String symbol;
    @NotEmpty
    private String description;
    @NotEmpty
    protected BigDecimal totalSupply;
    @NotEmpty
    protected int scale;
    @NotEmpty
    protected @Valid Hash originatorHash;
    @NotEmpty
    protected @Valid SignatureData originatorSignature;

    protected OriginatorCurrencyData() {
    }

    public OriginatorCurrencyData(OriginatorCurrencyData originatorCurrencyData) {
        name = originatorCurrencyData.getName();
        symbol = originatorCurrencyData.getSymbol();
        description = originatorCurrencyData.getDescription();
        totalSupply = originatorCurrencyData.getTotalSupply();
        scale = originatorCurrencyData.getScale();
        originatorHash = originatorCurrencyData.getOriginatorHash();
        originatorSignature = originatorCurrencyData.getOriginatorSignature();
    }

    public Hash calculateHash(){
        return CryptoHelper.cryptoHash(symbol.getBytes(), 224);
    }
}
