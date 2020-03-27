package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Data
public class CurrencyTypeData implements ISignable, ISignValidatable, Serializable {

    @NotEmpty
    private String symbol;
    @NotNull
    private CurrencyType currencyType;
    @NotNull
    private Instant createTime;
    private CurrencyRateSourceType currencyRateSourceType;
    private String rateSource;
    private String protectionModel;
    @NotNull
    private @Valid Hash originatorHash;
    @NotNull
    private @Valid SignatureData originatorSignature;

    protected CurrencyTypeData() {
    }

    public CurrencyTypeData(CurrencyType currencyType, Instant createTime) {
        this.currencyType = currencyType;
        this.createTime = createTime;
    }

    @JsonIgnore
    @Override
    public SignatureData getSignature() {
        return originatorSignature;
    }

    @Override
    public Hash getSignerHash() {
        return originatorHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.originatorHash = signerHash;
    }

    @JsonIgnore
    @Override
    public void setSignature(SignatureData signature) {
        this.originatorSignature = signature;
    }

    public Hash calculateHash() {
        return CryptoHelper.cryptoHash(symbol.getBytes(), 224);
    }
}
