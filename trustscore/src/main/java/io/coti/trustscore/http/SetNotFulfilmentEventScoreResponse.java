package io.coti.trustscore.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.CompensableEventScoreType;
import io.coti.trustscore.data.Enums.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetNotFulfilmentEventScoreResponse extends BaseResponse {

    private String userHash;
    private int eventType;
    private String notFulfilmentEventScoreType;

    public SetNotFulfilmentEventScoreResponse(Hash userHash,
                                              EventType eventType,
                                              CompensableEventScoreType compensableEventScoreType) {
        this.userHash = userHash.toHexString();
        this.eventType = eventType.getValue();
        this.notFulfilmentEventScoreType = compensableEventScoreType.toString();
    }
}
