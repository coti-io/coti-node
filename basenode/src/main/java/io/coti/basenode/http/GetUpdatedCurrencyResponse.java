package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

@Data
public class GetUpdatedCurrencyResponse extends BaseResponse implements ISignable, ISignValidatable {

    @NotNull
    private Map<CurrencyType, HashSet<CurrencyData>> currencyDataByType;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

    public GetUpdatedCurrencyResponse() {
        this.currencyDataByType = new EnumMap<CurrencyType, HashSet<CurrencyData>>(CurrencyType.class);
    }
}
