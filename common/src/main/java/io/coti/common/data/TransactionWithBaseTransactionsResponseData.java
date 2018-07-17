package io.coti.common.data;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionWithBaseTransactionsResponseData {

    private TransactionResponseData transactionResponseData;
    private List<BaseTransactionResponseData> baseTransactionsResponseData;

    public TransactionWithBaseTransactionsResponseData(TransactionData transactionData)
    {
        //, List<BaseTransactionResponseData> baseTransactionResponseData

        this.baseTransactionsResponseData = new ArrayList<>();
        this.transactionResponseData = new TransactionResponseData(transactionData);

        for (BaseTransactionData bxData: transactionData.getBaseTransactions()
             ) {
            this.baseTransactionsResponseData.add(new BaseTransactionResponseData(bxData));
        }


    }


}
