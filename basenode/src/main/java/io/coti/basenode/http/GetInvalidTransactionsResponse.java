package io.coti.basenode.http;


import io.coti.basenode.data.InvalidTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetInvalidTransactionsResponse extends BaseResponse {

    protected ArrayList<InvalidTransactionData> invalidtransactionsDataList;

    protected GetInvalidTransactionsResponse() {

    }

    public GetInvalidTransactionsResponse(List<InvalidTransactionData> invalidTransactions) {
        this.invalidtransactionsDataList = new ArrayList<>();
        invalidtransactionsDataList.addAll(invalidTransactions);
    }

}

