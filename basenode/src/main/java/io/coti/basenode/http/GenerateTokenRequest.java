package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class GenerateTokenRequest extends SerializableRequest implements ISignable, ISignValidatable {

    private Hash hash;
    @NotEmpty
    private Hash transactionHash;

    //TODO 9/22/2019 astolia: change to OriginatorCurrencyData
    @NotEmpty
    private CurrencyData currencyData;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return currencyData.getOriginatorHash();
    }

    @Override
    public void setSignerHash(Hash signerHash) {

    }
}
