package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class InsertChargeBackFrequencyBasedScoreResponse extends BaseResponse {
    private static final long serialVersionUID = 4955685749215120336L;
    private String userHash;
    private String eventIdentifier;
    private String transactionHash;
    private String amount;

    public InsertChargeBackFrequencyBasedScoreResponse(String userHash, String eventIdentifier, String transactionHash, String amount) {
        super();
        this.userHash = userHash;
        this.eventIdentifier = eventIdentifier;
        this.transactionHash = transactionHash;
        this.amount = amount;
    }
}
