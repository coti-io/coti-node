package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.EventRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InsertEventScoreRequest extends SignedRequest {
    private static final long serialVersionUID = -7542519785532048314L;
    @NotNull
    public EventRequestType eventType;
    @NotNull
    public Hash eventIdentifier;
}