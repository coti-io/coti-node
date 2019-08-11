package io.coti.historynode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.historynode.http.data.HistoryTransactionResponseData;
import lombok.Data;

@Data
public class HistoryTransactionResponse extends BaseResponse {

    private HistoryTransactionResponseData historyTransactionResponseData;

    public HistoryTransactionResponse(HistoryTransactionResponseData historyTransactionResponseData) {
        this.historyTransactionResponseData = historyTransactionResponseData;
    }

    public HistoryTransactionResponse(String status, HistoryTransactionResponseData historyTransactionResponseData) {
        super(status);
        this.historyTransactionResponseData = historyTransactionResponseData;
    }
}
