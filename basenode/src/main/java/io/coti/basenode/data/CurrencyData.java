package io.coti.basenode.data;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Data
public class CurrencyData extends OriginatorCurrencyData implements IPropagatable {

    private static final long serialVersionUID = -6040661248851422391L;
    private Hash hash;
    private CurrencyTypeData currencyTypeData;
    private Instant createTime;
    private Hash currencyGeneratingTransactionHash;
    private Hash currencyLastTypeChangingTransactionHash;
    private boolean confirmed;

    public CurrencyData() {
        super();
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData) {
        super(originatorCurrencyData);
    }

    public CurrencyData(OriginatorCurrencyData originatorCurrencyData, CurrencyTypeData currencyTypeData, Instant createTime,
                        Hash currencyGeneratingTransactionHash, Hash currencyLastTypeChangingTransactionHash, boolean confirmed) {
        super(originatorCurrencyData);
        setHash();
        this.createTime = createTime;
        this.currencyTypeData = currencyTypeData;
        this.currencyGeneratingTransactionHash = currencyGeneratingTransactionHash;
        this.currencyLastTypeChangingTransactionHash = currencyLastTypeChangingTransactionHash;
        this.confirmed = confirmed;
    }

    public void setHash() {
        hash = OriginatorCurrencyCrypto.calculateHash(this.symbol);
    }

    public boolean isNativeCurrency() {
        return getCurrencyTypeData() != null && getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN);
    }
}
