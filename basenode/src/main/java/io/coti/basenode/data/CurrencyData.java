package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@Data
public class CurrencyData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    @NotEmpty
    private String name;
    @NotEmpty
    private String symbol;
    private CurrencyTypeData currencyTypeData;
    @NotEmpty
    private String description;
    private BigDecimal totalSupply;
    private int scale;
    private Instant creationTime;
    private Hash originatorHash;
    private SignatureData originatorSignature;
    private Hash registrarHash;

    private SignatureData registrarSignature;

    public CurrencyData() {
    }

    public void setHash() {
        byte[] nameInBytes = name.getBytes();
        byte[] symbolInBytes = symbol.getBytes();
        byte[] concatDataFields = ByteBuffer.allocate(nameInBytes.length + symbolInBytes.length).
                put(nameInBytes).put(symbolInBytes).array();
        hash = CryptoHelper.cryptoHash(concatDataFields, 224);
    }

    public void setName(String name) {
        if (name.length() != name.trim().length()) {
            log.error("Attempted to set an invalid currency name with spaces at the start or the end {}.", name);
            //TODO 9/1/2019 tomer: Throw an exception or return boolean
            return;
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9]+").matcher(word).matches()) {
                log.error("Attempted to set an invalid currency name with the word {}.", name);
                //TODO 9/1/2019 tomer: Throw an exception or return boolean
                return;
            }
        }
        this.name = name;
    }

    public void setSymbol(String symbol) {
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            log.error("Attempted to set an invalid currency symbol of {}.", symbol);
            return;
            //TODO 9/1/2019 tomer: Throw an exception or return boolean
        } else {
            this.symbol = symbol;
        }
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
