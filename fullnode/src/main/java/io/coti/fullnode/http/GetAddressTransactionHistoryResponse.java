package io.coti.fullnode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionsResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetAddressTransactionHistoryResponse extends GetTransactionsResponse {

    private int totalNumberOfTransactions;
    private int missingNumberOfTransactions;

    public GetAddressTransactionHistoryResponse(List<TransactionData> transactionsData, int totalNumberOfTransactions) {
        super(transactionsData);
        this.totalNumberOfTransactions = totalNumberOfTransactions;
        missingNumberOfTransactions = totalNumberOfTransactions - transactionsData.size();
    }
}

