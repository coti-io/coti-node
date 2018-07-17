package io.coti.common.http;



import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionWithBaseTransactionsResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetAddressTransactionHistory extends Response {
    private List<TransactionWithBaseTransactionsResponseData> transactionsResponseDataData;

    public GetAddressTransactionHistory(List<TransactionData> transactionsData) {
        super();
        transactionsResponseDataData = new ArrayList<>();
        for (TransactionData transactionData: transactionsData
             ) {
            transactionsResponseDataData.add(new TransactionWithBaseTransactionsResponseData(transactionData));
        }


    }
}

