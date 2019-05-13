package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;


@Data
public class KycEventData extends EventData {

    private static final long serialVersionUID = -7912516118739590690L;
    private Hash userHash;
    private EventType eventType;

    public KycEventData(InsertEventRequest request) {
        super(request);
    }

}
