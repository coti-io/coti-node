package io.coti.historynode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.util.Map;

@Data
public class HistoryTransactionResponseData implements IResponseData {

    private Map<Hash, TransactionData> historyTransactionResultMap;

    public HistoryTransactionResponseData(Map<Hash, TransactionData> historyTransactionResultMap) {
        this.historyTransactionResultMap = historyTransactionResultMap;
    }
}
