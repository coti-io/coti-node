package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionEventData extends EventData {
    private int numberOfTransactions;
    private BigDecimal turnOver;
    private BigDecimal balance;
    private TransactionData transactionData;

    public TransactionEventData(TransactionData transactionData, int numberOfTransactions, BigDecimal turnOver, BigDecimal balance) {

        this.numberOfTransactions = numberOfTransactions;
        this.turnOver = turnOver;
        this.transactionData = transactionData;
        this.balance = balance;
    }

    @Override
    public int hashCode() {
        return transactionData.getHash().hashCode();
    }

    @Override
    public Hash getHash() {
        return this.transactionData.getHash();
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionData.setHash(hash);
    }
}
