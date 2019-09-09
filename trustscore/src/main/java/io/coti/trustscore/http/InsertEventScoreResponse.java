package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class InsertEventScoreResponse extends BaseResponse {
    private static final long serialVersionUID = 601252520554001209L;
    private String userHash;
    private String eventType;
    private String eventIdentifier;

    public InsertEventScoreResponse(String userHash, String eventType, String eventIdentifier) {
        super();
        this.userHash = userHash;
        this.eventType = eventType;
        this.eventIdentifier = eventIdentifier;

    }
}
