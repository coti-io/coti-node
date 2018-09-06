package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;

@Data
public class GetTransactionResponse extends BaseResponse {
    private TransactionResponseData transactionData;

    public GetTransactionResponse(TransactionResponseData transactionResponseData) {
        super();
        this.transactionData = transactionResponseData;
    }

    public GetTransactionResponse(String status, String message) {
        super(message);
        this.status = status;
    }
}

