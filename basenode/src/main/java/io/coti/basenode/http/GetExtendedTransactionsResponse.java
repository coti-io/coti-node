package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.ExtendedTransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetExtendedTransactionsResponse extends BaseResponse {

    private List<ExtendedTransactionResponseData> transactionsData;

    public GetExtendedTransactionsResponse(List<TransactionData> transactionsData) {
        this.transactionsData = new ArrayList<>();

        for (TransactionData transactionData : transactionsData) {
            this.transactionsData.add(new ExtendedTransactionResponseData(transactionData));
        }

    }

}
