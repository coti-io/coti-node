package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTotalTransactionsResponse extends BaseResponse {

    private int totalTransactions;

    public GetTotalTransactionsResponse(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
