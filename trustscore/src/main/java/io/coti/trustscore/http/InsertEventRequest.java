package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Request;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.scoreenums.BehaviorEventsScoreType;
import io.coti.trustscore.data.scoreenums.CompensableEventScoreType;
import io.coti.trustscore.data.scoreenums.EventType;
import io.coti.trustscore.data.scoreenums.HighFrequencyEventScoreType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class InsertEventRequest extends Request {

    @NotNull
    public Instant eventDate;
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
    private double amount;
    private Hash otherUserHash;

}

// todo delete
