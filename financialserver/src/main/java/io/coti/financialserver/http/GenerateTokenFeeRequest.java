package io.coti.financialserver.http;

import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.http.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateTokenFeeRequest extends Request {

    @NotNull
    private @Valid OriginatorCurrencyData originatorCurrencyData;
    @NotNull
    private @Valid CurrencyTypeData currencyTypeData;

}
