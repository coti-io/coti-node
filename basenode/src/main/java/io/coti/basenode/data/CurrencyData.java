package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CurrencyException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@Data
public class CurrencyData extends OriginatorCurrencyData implements IPropagatable {

    private Hash hash;
    private CurrencyTypeData currencyTypeData;
    private Instant createTime;
    private Hash currencyGeneratingTransactionHash;
    private Hash currencyLastTypeChangingTransactionHash;

    public CurrencyData() {
        super();
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData) {
        super(originatorCurrencyData);
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData, CurrencyTypeData currencyTypeData, Instant createTime, Hash currencyGeneratingTransactionHash, Hash currencyLastTypeChangingTransactionHash) {
        super(originatorCurrencyData);
        this.createTime = createTime;
        this.currencyTypeData = currencyTypeData;
        this.currencyGeneratingTransactionHash = currencyGeneratingTransactionHash;
        this.currencyLastTypeChangingTransactionHash = currencyLastTypeChangingTransactionHash;
    }

    public void setHash() {
        hash = super.calculateHash();
    }

    @Override
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

    @Override
    public void setSymbol(String symbol) {
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency symbol of %s.", symbol));
        }
        this.symbol = symbol;
    }
}
