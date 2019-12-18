package io.coti.trustscore.data.events;

import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class ChargeBackEventsData extends EventData {

    private static final long serialVersionUID = 6615286089154223293L;
    private TransactionData transactionData;

    public ChargeBackEventsData(InsertEventRequest request) {
        super(request);
        this.transactionData = request.getTransactionData();
        super.setSignatureData(request.getSignature());
    }
}
