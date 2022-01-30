package io.coti.financialserver.http.data;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
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

    public GetCurrencyResponseData(String currencyName, String currencySymbol) {
        this.hash = OriginatorCurrencyCrypto.calculateHash(currencySymbol).toHexString();
        this.name = currencyName;
        this.symbol = currencySymbol;
        this.iconURL = "placeHolder";
    }
}
