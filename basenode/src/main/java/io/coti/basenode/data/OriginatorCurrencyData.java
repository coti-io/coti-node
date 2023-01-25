package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OriginatorCurrencyData implements ISignable, ISignValidatable, Serializable {

    private static final long serialVersionUID = 6995268770045990662L;
    @SuppressWarnings("java:S5998")
    @Pattern(regexp = "^(?=.{1,64}$)(?:[A-Za-z0-9]+[-. ])*[A-Za-z0-9]+$")
    protected String name;
    @Pattern(regexp = "[A-Za-z0-9]{1,6}+")
    protected String symbol;
    @Length(min = 5, max = 255)
    private String description;
    @Range(min = 1L, max = 100000000000L)
    protected BigDecimal totalSupply;
    @Range(min = 8, max = 8)
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

}
