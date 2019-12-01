package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class GeneratedTokenResponseData {

    private String transactionHash;
    private GeneratedTokenResponseCurrencyData token;
    private boolean approved;

    public GeneratedTokenResponseData(Hash transactionHash, CurrencyData token, boolean approved) {
        this.transactionHash = transactionHash.toString();
        if (token == null) {
            this.token = null;
        } else {
            this.token = new GeneratedTokenResponseCurrencyData(token);
        }

        this.approved = approved;
    }
}
