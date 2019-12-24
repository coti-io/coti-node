package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.tsenums.EventRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InsertEventScoreRequest extends SignedRequest {
    @NotNull
    private EventRequestType eventType;
    @NotNull
    private Hash eventIdentifier;
}