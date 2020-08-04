package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import lombok.Data;

@Data
public class GetNativeCurrencyResponse extends BaseResponse {
    private CurrencyData nativeCurrency;

    public GetNativeCurrencyResponse() {
    }

    public GetNativeCurrencyResponse(CurrencyData nativeCurrency) {
        this.nativeCurrency = nativeCurrency;
    }
}
