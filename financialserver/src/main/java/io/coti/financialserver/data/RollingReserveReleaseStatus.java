package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RollingReserveReleaseStatus implements Serializable {

    private BigDecimal initialAmount;
    private BigDecimal returnedAmount;
    private List<Hash> paymentTransactions;
    private List<Hash> returnTransactions;
    private RollingReserveReceiver rollingReserveReceiver;

    public RollingReserveReleaseStatus() {
        paymentTransactions = new ArrayList<>();
    }

    public void addToInitialAmount(BigDecimal amount) {
        initialAmount = initialAmount.add(amount);
    }
}
