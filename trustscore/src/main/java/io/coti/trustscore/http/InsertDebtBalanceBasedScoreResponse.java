package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class InsertDebtBalanceBasedScoreResponse extends BaseResponse {
    private String userHash;
    private String eventType;
    private String amount;
    private String eventIdentifier;
    private String otherUserHash;

    public InsertDebtBalanceBasedScoreResponse(String userHash, String eventType, String amount, String eventIdentifier, String otherUserHash) {
        super();
        this.userHash = userHash;
        this.eventType = eventType;
        this.amount = amount;
        this.eventIdentifier = eventIdentifier;
        this.otherUserHash = otherUserHash;
    }
}
