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

    protected List<TransactionResponseData> transactionsData;

    protected GetTransactionsResponse() {

    }

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        this.transactionsData = new ArrayList<>();

        for (TransactionData transactionData : transactionsData) {
            this.transactionsData.add(new TransactionResponseData(transactionData));
        }

    }

}

