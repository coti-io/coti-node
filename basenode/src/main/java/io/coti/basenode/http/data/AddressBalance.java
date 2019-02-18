package io.coti.basenode.http.data;

import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddressBalance {

    public BigDecimal addressBalance;
    public BigDecimal addressPreBalance;
    public List<TransactionData> preBalanceGapTransactions;

    public AddressBalance(BigDecimal addressBalance, BigDecimal addressPreBalance) {
        this.addressBalance = addressBalance;
        this.addressPreBalance = addressPreBalance;
    }

    public AddressBalance(BigDecimal addressBalance, BigDecimal addressPreBalance, List<TransactionData> preBalanceGapTransactions) {
        this.addressBalance = addressBalance;
        this.addressPreBalance = addressPreBalance;
        this.preBalanceGapTransactions = preBalanceGapTransactions;
    }
}
