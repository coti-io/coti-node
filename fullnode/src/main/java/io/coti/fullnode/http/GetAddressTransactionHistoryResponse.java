package io.coti.fullnode.http;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;

import java.util.List;
import java.util.Vector;

@Data
public class GetAddressTransactionHistoryResponse extends BaseResponse {
    private List<TransactionResponseData> transactionsData;

    public GetAddressTransactionHistoryResponse(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = new Vector<>();
        for (TransactionData transactionData : transactionsData
                ) {
            this.transactionsData.add(new TransactionResponseData(transactionData));
        }


    }
}

