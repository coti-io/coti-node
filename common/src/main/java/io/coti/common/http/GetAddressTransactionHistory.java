package io.coti.common.http;


import io.coti.common.data.TransactionData;
import lombok.Data;

import java.util.List;

@Data
public class GetAddressTransactionHistory extends Response {
    private List<TransactionData> transactionsData;

    public GetAddressTransactionHistory(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = transactionsData;
    }
}

