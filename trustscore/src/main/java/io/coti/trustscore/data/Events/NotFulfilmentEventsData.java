package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.Enums.CompensableEventScoreType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class NotFulfilmentEventsData extends EventData {
    private CompensableEventScoreType compensableEventScoreType;
    private Hash clientUserHash;
    private double debtAmount;

    public NotFulfilmentEventsData(InsertEventRequest request) {
        super(request);
        this.compensableEventScoreType = request.getCompensableEventScoreType();
        this.clientUserHash = request.getOtherUserHash();
        this.debtAmount = request.getDebtAmount();
        super.setSignatureData(request.getSignature());
    }
}