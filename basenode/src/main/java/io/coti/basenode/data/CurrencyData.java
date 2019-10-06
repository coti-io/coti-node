package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.exceptions.CurrencyException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@Data
public class CurrencyData extends OriginatorCurrencyData implements IPropagatable, ISignable, ISignValidatable {

    @NotNull
    private @Valid Hash hash;
    @NotNull
    private @Valid CurrencyTypeData currencyTypeData;
    @NotNull
    private Instant creationTime;
    @NotNull
    private @Valid Hash registrarHash;
    @NotNull
    private @Valid SignatureData registrarSignature;

    public CurrencyData() {
        super();
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData) {
        super(originatorCurrencyData);
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData, CurrencyTypeData currencyTypeData) {
        super(originatorCurrencyData);
        creationTime = currencyTypeData.creationTime;
        this.currencyTypeData = currencyTypeData;
        setHash();
    }

    public void setHash() {
        hash = super.calculateHash();
    }

    public void setName(String name) {
        if (name.length() != name.trim().length()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency name with spaces at the start or the end %s.", name));
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9]+").matcher(word).matches()) {
                throw new CurrencyException(String.format("Attempted to set an invalid currency name with the word %s.", name));
            }
        }
        this.name = name;
    }

    public void setSymbol(String symbol) {
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency symbol of %s.", symbol));
        }
        this.symbol = symbol;
    }

    @JsonIgnore
    @Override
    public SignatureData getSignature() {
        return registrarSignature;
    }

    @JsonIgnore
    @Override
    public Hash getSignerHash() {
        return registrarHash;
    }

    @JsonIgnore
    @Override
    public void setSignerHash(Hash signerHash) {
        this.registrarHash = signerHash;
    }

    @JsonIgnore
    @Override
    public void setSignature(SignatureData signature) {
        this.registrarSignature = signature;
    }
}
