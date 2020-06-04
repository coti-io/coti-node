package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTransactionResponse extends BaseResponse {

    private TransactionResponseData transactionData;

    public GetTransactionResponse(TransactionResponseData transactionResponseData) {
        super();
        this.transactionData = transactionResponseData;
    }
}

