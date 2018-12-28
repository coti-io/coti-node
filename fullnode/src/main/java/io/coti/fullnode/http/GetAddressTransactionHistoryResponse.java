package io.coti.fullnode.http;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class GetAddressTransactionHistoryResponse extends BaseResponse {
    private List<TransactionResponseData> transactionsData;

    public GetAddressTransactionHistoryResponse(List<TransactionData> transactionsData) throws Exception{
        super();

        this.transactionsData = new ArrayList<>();
        try {
        for(TransactionData transactionData: transactionsData)  {
                this.transactionsData.add(new TransactionResponseData(transactionData));

        }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }
}

