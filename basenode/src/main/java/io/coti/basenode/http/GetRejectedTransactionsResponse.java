package io.coti.basenode.http;


import io.coti.basenode.http.data.RejectedTransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetRejectedTransactionsResponse extends BaseResponse {

    protected ArrayList<RejectedTransactionResponseData> rejectedTransactionsDataList;

    protected GetRejectedTransactionsResponse() {

    }

    public GetRejectedTransactionsResponse(List<RejectedTransactionResponseData> rejectedTransactions) {
        this.rejectedTransactionsDataList = new ArrayList<>();
        rejectedTransactionsDataList.addAll(rejectedTransactions);
    }

}