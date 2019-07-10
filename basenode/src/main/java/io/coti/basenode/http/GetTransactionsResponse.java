package io.coti.basenode.http;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class GetTransactionsResponse extends BaseResponse {

    protected List<TransactionResponseData> transactionsData;

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        super();

        this.transactionsData = new ArrayList<>();

        for (TransactionData transactionData : transactionsData) {
            this.transactionsData.add(new TransactionResponseData(transactionData));
        }

    }

}

