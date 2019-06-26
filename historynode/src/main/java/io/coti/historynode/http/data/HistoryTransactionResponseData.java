package io.coti.historynode.http.data;

import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.util.List;

@Data
public class HistoryTransactionResponseData {

    private List<TransactionData> HistoryTransactionResults;

    public HistoryTransactionResponseData(List<TransactionData> historyTransactionResults) {
        HistoryTransactionResults = historyTransactionResults;
    }
}
