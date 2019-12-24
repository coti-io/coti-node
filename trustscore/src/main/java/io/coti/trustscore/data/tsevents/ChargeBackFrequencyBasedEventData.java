package io.coti.trustscore.data.tsevents;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.http.InsertChargeBackFrequencyBasedScoreRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeBackFrequencyBasedEventData extends FrequencyBasedEventData {
    private static final long serialVersionUID = -781883843990870735L;
    private Hash transactionHash;
    private BigDecimal amount;

    public ChargeBackFrequencyBasedEventData() {
    }

// Been created using reflection
    public ChargeBackFrequencyBasedEventData(InsertChargeBackFrequencyBasedScoreRequest request) {
        super(request);
        this.setTransactionHash(request.getTransactionHash());
        this.setAmount(request.getAmount());
    }
}
