package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@Slf4j
@Data
public class GetTransactionResponse extends BaseResponse {

    @Valid
    private TransactionData transactionData;

    public GetTransactionResponse() {}

    public GetTransactionResponse(TransactionData transactionData) {
        this.transactionData = transactionData;
    }
}
