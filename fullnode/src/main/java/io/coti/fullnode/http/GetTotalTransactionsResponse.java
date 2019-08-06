package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetTotalTransactionsResponse extends BaseResponse {

    private int totalTransactions;

    public GetTotalTransactionsResponse(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
