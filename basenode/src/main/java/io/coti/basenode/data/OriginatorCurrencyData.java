package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OriginatorCurrencyData implements ISignable, ISignValidatable, Serializable {

    @NotEmpty
    protected String name;
    @NotEmpty
    protected String symbol;
    @NotEmpty
    private String description;
    @DecimalMin(value = "0")
    protected BigDecimal totalSupply;
    @Range(min = 0, max = 12)
    protected int scale;
    @NotNull
    protected @Valid Hash originatorHash;
    @NotNull
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

    @JsonIgnore
    @Override
    public SignatureData getSignature() {
        return originatorSignature;
    }

    @JsonIgnore
    @Override
    public Hash getSignerHash() {
        return originatorHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.originatorHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.originatorSignature = signature;
    }

    public Hash calculateHash() {
        return CryptoHelper.cryptoHash(symbol.getBytes(), 224);
    }
}
