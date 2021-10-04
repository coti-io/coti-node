package io.coti.basenode.http;


import io.coti.basenode.http.data.InvalidTransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetInvalidTransactionsResponse extends BaseResponse {

    protected ArrayList<InvalidTransactionResponseData> invalidtransactionsDataList;

    protected GetInvalidTransactionsResponse() {

    }

    public GetInvalidTransactionsResponse(List<InvalidTransactionResponseData> invalidTransactions) {
        this.invalidtransactionsDataList = new ArrayList<>();
        invalidtransactionsDataList.addAll(invalidTransactions);
    }

}

