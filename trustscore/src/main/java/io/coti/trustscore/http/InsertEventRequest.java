package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Request;
import io.coti.trustscore.data.Enums.*;
import io.coti.trustscore.data.Events.KycEventData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class InsertEventRequest extends Request {

    @NotNull
    public Date eventDate;

    @NotNull
    public Hash userHash;

    @NotNull
    public EventType eventType;
    @NotNull
    public Hash signerHash;
    @NotNull
    public SignatureData signature;

    public Hash uniqueIdentifier;

    private BehaviorEventsScoreType behaviorEventsScoreType;
    private InitialTrustScoreType initialTrustScoreType;
    private HighFrequencyEventScoreType highFrequencyEventScoreType;
    private CompensableEventScoreType compensableEventScoreType;
    private TransactionData transactionData;
    private double score;
    private double debtAmount;
    private Hash otherUserHash;

}
