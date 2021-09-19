package io.coti.fullnode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionsResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetAddressTransactionHistoryResponse extends GetTransactionsResponse {

    public GetAddressTransactionHistoryResponse(List<TransactionData> transactionsData, int totalNumberOfTransactions) {
        super(transactionsData, totalNumberOfTransactions);
    }

    public GetAddressTransactionHistoryResponse(List<TransactionData> transactionsData) {
        super(transactionsData);
    }
}

