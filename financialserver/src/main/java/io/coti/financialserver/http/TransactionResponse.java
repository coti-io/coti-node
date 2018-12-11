package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.http.BaseResponse;

@Data
public class TransactionResponse extends BaseResponse {

    public TransactionResponse(String status) {
        super(status);
    }
}
