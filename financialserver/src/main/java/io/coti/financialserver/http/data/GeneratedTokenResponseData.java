package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class GeneratedTokenResponseData {

    private Hash transactionHash;
    private CurrencyData token;
    private boolean approved;

    public GeneratedTokenResponseData(Hash transactionHash, CurrencyData token, boolean approved) {
        this.transactionHash = transactionHash;
        this.token = token;
        this.approved = approved;
    }
}
