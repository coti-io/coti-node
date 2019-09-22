package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;

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
