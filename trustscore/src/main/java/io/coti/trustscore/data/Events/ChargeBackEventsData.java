package io.coti.trustscore.data.Events;

import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChargeBackEventsData extends EventData {

    private static final long serialVersionUID = 6615286089154223293L;
    private TransactionData transactionData;

    public ChargeBackEventsData(InsertEventRequest request) {
        super(request);
        this.transactionData = request.getTransactionData();
        super.setSignatureData(request.getSignature());
    }
}
