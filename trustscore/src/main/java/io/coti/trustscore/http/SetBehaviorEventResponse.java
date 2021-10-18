package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.Enums.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetBehaviorEventResponse extends BaseResponse {
    private String userHash;
    private int eventType;
    private String behaviorEventsScoreType;
    private String transactionDataHash;

    public SetBehaviorEventResponse(Hash userHash, EventType eventType, BehaviorEventsScoreType behaviorEventsScoreType, Hash transactionDataHash) {
        this.userHash = userHash.toHexString();
        this.eventType = eventType.getValue();
        this.behaviorEventsScoreType = behaviorEventsScoreType.toString();
        this.transactionDataHash = (transactionDataHash != null) ? transactionDataHash.toHexString() : null;
    }

}
