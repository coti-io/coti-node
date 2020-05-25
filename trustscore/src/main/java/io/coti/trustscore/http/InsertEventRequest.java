package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.IRequest;
import io.coti.trustscore.data.Enums.*;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class InsertEventRequest implements IRequest {

    @NotNull
    private Instant eventDate;
    @NotNull
    private Hash userHash;
    @NotNull
    private EventType eventType;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;
    private Hash uniqueIdentifier;
    private BehaviorEventsScoreType behaviorEventsScoreType;
    private InitialTrustScoreType initialTrustScoreType;
    private HighFrequencyEventScoreType highFrequencyEventScoreType;
    private CompensableEventScoreType compensableEventScoreType;
    private TransactionData transactionData;
    private double score;
    private double debtAmount;
    private Hash otherUserHash;

}
