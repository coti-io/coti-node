package io.coti.historynode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.util.HashMap;

@Data
public class HistoryTransactionResponseData {

    private HashMap<Hash, TransactionData> HistoryTransactionResults;

    public HistoryTransactionResponseData(HashMap<Hash, TransactionData> historyTransactionResults) {
        HistoryTransactionResults = historyTransactionResults;
    }
}
