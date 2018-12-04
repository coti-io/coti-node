package io.coti.trustscore.data.Events;

import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class ChargeBackEventsData extends EventData {
    private TransactionData transactionData;

    public ChargeBackEventsData(InsertEventRequest request) {
        super(request);
        this.transactionData = request.getTransactionData();
        super.setSignatureData(request.getSignature());
    }
}
