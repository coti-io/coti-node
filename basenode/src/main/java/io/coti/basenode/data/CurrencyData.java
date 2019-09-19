package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.exceptions.CurrencyException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@Data
public class CurrencyData extends OriginatorCurrencyData implements IPropagatable, ISignable, ISignValidatable {

    @NotEmpty
    private @Valid Hash hash;
    @NotEmpty
    private @Valid CurrencyTypeData currencyTypeData;
    @NotEmpty
    private Instant creationTime;
    @NotEmpty
    private @Valid Hash registrarHash;
    @NotEmpty
    private @Valid SignatureData registrarSignature;

    public CurrencyData() {
        super();
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData) {
        super(originatorCurrencyData);
    }

    public void setHash() {
        hash = CryptoHelper.cryptoHash(symbol.getBytes(), 224);
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
