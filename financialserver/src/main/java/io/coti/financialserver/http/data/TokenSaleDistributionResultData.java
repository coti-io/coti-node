package io.coti.financialserver.http.data;

import lombok.Data;

@Data
public class TokenSaleDistributionResultData {
    private String tokenSale;
    private boolean success;

    public TokenSaleDistributionResultData(String tokenSale, boolean success) {
        this.tokenSale = tokenSale;
        this.success = success;
    }

}
