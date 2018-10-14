package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Request;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.CentralEventData;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class InsertTrustScoreEventRequest extends Request {

    @NotNull
    public Date eventDate;

    @NotNull
    public Hash userHash;

    @NotNull
    public EventType eventType;

    @NotNull
    public @Valid SignatureData signature;

    public CentralEventData convertToCentralEvent(Hash kycPublicHash) {
        return new CentralEventData(userHash, eventDate, eventType, signature);
    }

}
