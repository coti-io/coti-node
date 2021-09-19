package io.coti.basenode.http;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTransactionsResponse extends BaseResponse {

    protected int totalNumberOfTransactions;

    protected int missingNumberOfTransactions;

    protected ArrayList<TransactionResponseData> transactionsData;

    protected GetTransactionsResponse() {

    }

    public GetTransactionsResponse(List<TransactionData> transactionsData, int numberOfTotalTransactions) {
        totalNumberOfTransactions = numberOfTotalTransactions;
        missingNumberOfTransactions = numberOfTotalTransactions - transactionsData.size();
        createTransactionsDataArray(transactionsData);
    }

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        createTransactionsDataArray(transactionsData);
    }

    private void createTransactionsDataArray(List<TransactionData> transactionsData) {
        this.transactionsData = new ArrayList<>();

        for (TransactionData transactionData : transactionsData) {
            this.transactionsData.add(new TransactionResponseData(transactionData));
        }

    }

}

