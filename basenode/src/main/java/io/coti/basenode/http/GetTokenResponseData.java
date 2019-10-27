package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class GetTokenResponseData extends BaseResponse {

    @NotEmpty
    private String hash;
    @NotEmpty
    protected String name;
    @NotEmpty
    protected String symbol;
    @NotEmpty
    protected String iconURL;

    public GetTokenResponseData(CurrencyData currencyData) {
        this.hash = currencyData.getHash().toHexString();
        this.name = currencyData.getName();
        this.symbol = currencyData.getSymbol();
        this.iconURL = "placeHolder";
    }
}
