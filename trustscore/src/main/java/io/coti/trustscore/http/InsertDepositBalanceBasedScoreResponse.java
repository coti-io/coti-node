package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class InsertDepositBalanceBasedScoreResponse extends BaseResponse {
    private String userHash;
    private String eventType;
    private String amount;
    private String eventIdentifier;

    public InsertDepositBalanceBasedScoreResponse(String userHash, String eventType, String amount, String eventIdentifier) {
        super();
        this.userHash = userHash;
        this.eventType = eventType;
        this.amount = amount;
        this.eventIdentifier = eventIdentifier;
    }
}
