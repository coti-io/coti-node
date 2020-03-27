package io.coti.financialserver.http;

import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GenerateTokenFeeRequest extends Request implements ISignValidatable {

    @NotNull
    private @Valid OriginatorCurrencyData originatorCurrencyData;
    @NotNull
    private @Valid CurrencyTypeData currencyTypeData;

    @Override
    public SignatureData getSignature() {
        return originatorCurrencyData.getOriginatorSignature();
    }

    @Override
    public Hash getSignerHash() {
        return originatorCurrencyData.getOriginatorHash();
    }

}
