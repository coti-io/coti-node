package io.coti.fullnode.http;

import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetAddressTransactionHistoryResponse extends GetTransactionsResponse {

    private int totalNumberOfTransactions;
    private int missingNumberOfTransactions;

    public GetAddressTransactionHistoryResponse(List<TransactionResponseData> transactionsData, int totalNumberOfTransactions) {
        super();
        this.transactionsData = (ArrayList<TransactionResponseData>) transactionsData;
        setNumberOfTransactions(totalNumberOfTransactions);
    }

    private void setNumberOfTransactions(int totalNumberOfTransactions) {
        this.totalNumberOfTransactions = totalNumberOfTransactions;
        missingNumberOfTransactions = totalNumberOfTransactions - transactionsData.size();
    }
}

