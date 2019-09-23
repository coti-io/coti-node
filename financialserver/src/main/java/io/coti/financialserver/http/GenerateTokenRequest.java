package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class GenerateTokenRequest extends Request implements ISignValidatable {

    @NotEmpty
    private Hash transactionHash;
    @NotEmpty
    private OriginatorCurrencyData originatorCurrencyData;
    @NotNull
    private SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return originatorCurrencyData.getOriginatorHash();
    }
}
