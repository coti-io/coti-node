package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.GetCurrencyResponseData;
import lombok.Data;

import java.util.List;

@Data
public class GetCurrenciesResponse extends BaseResponse {

    private GetCurrencyResponseData nativeCurrency;
    private List<GetCurrencyResponseData> tokens;

    public GetCurrenciesResponse(GetCurrencyResponseData nativeCurrency, List<GetCurrencyResponseData> tokens) {
        this.nativeCurrency = nativeCurrency;
        this.tokens = tokens;
    }
}
