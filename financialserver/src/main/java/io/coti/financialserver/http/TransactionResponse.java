package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionResponse extends BaseResponse {

    public TransactionResponse(String status) {
        super(status);
    }
}
