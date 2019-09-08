package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import lombok.Data;

@Data
public class GetTransactionTrustScoreResponse extends BaseResponse {

    private TransactionTrustScoreResponseData transactionTrustScoreData;

    public GetTransactionTrustScoreResponse(TransactionTrustScoreResponseData transactionTrustScoreData) {
        this.transactionTrustScoreData = transactionTrustScoreData;
    }
}