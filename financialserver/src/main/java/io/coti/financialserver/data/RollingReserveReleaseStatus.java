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

    public RollingReserveReleaseStatus(BigDecimal initialAmount, Hash paymentTransaction) {
        this.paymentTransactions = new ArrayList<>();
        this.initialAmount = initialAmount;
        this.returnedAmount = new BigDecimal(0);
        this.paymentTransactions.add(paymentTransaction);
    }

    public void addToInitialAmount(BigDecimal amount) {
        initialAmount = initialAmount.add(amount);
    }

    public BigDecimal getRemainingAmount() {
        return (initialAmount.subtract(returnedAmount));
    }
}
