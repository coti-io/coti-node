package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.http.data.CurrencyDataForFee;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GenerateTokenFeeRequest extends Request {

    @NotNull
    private @Valid CurrencyDataForFee currencyData;

}
