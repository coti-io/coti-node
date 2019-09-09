package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertChargeBackFrequencyBasedScoreRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeBackFrequencyBasedScoreData extends FrequencyBasedScoreData {

    private static final long serialVersionUID = -781883843990870735L;
    private Hash transactionHash;
    private BigDecimal amount;

    public ChargeBackFrequencyBasedScoreData() {
    }

    public ChargeBackFrequencyBasedScoreData(InsertChargeBackFrequencyBasedScoreRequest request) {
        super(request, FinalScoreType.CHARGEBACK);
        this.setTransactionHash(request.getTransactionHash());
        this.setAmount(request.getAmount());
    }
}
