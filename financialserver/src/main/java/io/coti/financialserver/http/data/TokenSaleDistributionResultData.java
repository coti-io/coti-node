package io.coti.financialserver.http.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenSaleDistributionResultData implements Serializable {

    private String tokenSale;
    private boolean success;

    public TokenSaleDistributionResultData(String tokenSale, boolean success) {
        this.tokenSale = tokenSale;
        this.success = success;
    }

}
