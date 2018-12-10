package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class TransactionResponse extends BaseResponse {

    public TransactionResponse(String status) {
        super(status);
    }
}
