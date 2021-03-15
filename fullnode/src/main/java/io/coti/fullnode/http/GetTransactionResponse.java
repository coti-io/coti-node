package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTransactionResponse extends BaseResponse {

    protected TransactionResponseData transactionData;

    protected GetTransactionResponse() {
    }

    public GetTransactionResponse(TransactionResponseData transactionResponseData) {
        this.transactionData = transactionResponseData;
    }
}

