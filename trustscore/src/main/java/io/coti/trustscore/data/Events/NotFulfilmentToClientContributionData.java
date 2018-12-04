package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class NotFulfilmentToClientContributionData {
    private Hash clientHash;
    private double currentDebt;
    private double fine;
    private double tail;

    public NotFulfilmentToClientContributionData(Hash clientHash, double currentDebt, double fine, double tail) {
        this.clientHash = clientHash;
        this.currentDebt = currentDebt;
        this.fine = fine;
        this.tail = tail;
    }
}
