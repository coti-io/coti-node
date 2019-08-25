package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class GetCurrencyResponse extends BaseResponse implements ISignable, ISignValidatable {

    private Set<CurrencyData> currencyDataSet;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

    public GetCurrencyResponse() {
        this.currencyDataSet = new HashSet<>();
    }
}
