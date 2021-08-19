package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetCurrencyResponseData extends BaseResponse {

    private String hash;
    protected String name;
    protected String symbol;
    protected String iconURL;

    public GetCurrencyResponseData(CurrencyData currencyData) {
        this.hash = currencyData.getHash().toHexString();
        this.name = currencyData.getName();
        this.symbol = currencyData.getSymbol();
        this.iconURL = "placeHolder";
    }
}
