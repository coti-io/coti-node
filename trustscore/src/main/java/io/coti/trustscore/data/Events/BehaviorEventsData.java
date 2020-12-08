package io.coti.trustscore.data.Events;


import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BehaviorEventsData extends EventData {

    private static final long serialVersionUID = 5236619026397892238L;
    private TransactionData transactionData;
    private BehaviorEventsScoreType behaviorEventsScoreType;

    public BehaviorEventsData(InsertEventRequest request) {
        super(request);
        this.behaviorEventsScoreType = request.getBehaviorEventsScoreType();
        this.transactionData = request.getTransactionData();
        this.setEventSignature(request.getSignature());
    }

}
