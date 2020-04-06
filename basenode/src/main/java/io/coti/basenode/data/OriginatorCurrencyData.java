package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.exceptions.CurrencyException;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Pattern;

@Data
public class OriginatorCurrencyData implements ISignable, ISignValidatable, Serializable {

    private static final long serialVersionUID = 6995268770045990662L;
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

    public void validateName() {
        if (name.length() != name.trim().length()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency name with spaces at the start or the end %s.", name));
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9]+").matcher(word).matches()) {
                throw new CurrencyException(String.format("Attempted to set an invalid currency name with the word %s.", name));
            }
        }
    }

    public void validateSymbol() {
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency symbol of %s.", symbol));
        }
    }

}
