package io.coti.trustscore.data;

import lombok.Data;

@Data
public class TransactionTsData {
    private int numberOfTransactions;
    private double turnOver;
    private double balance;
}
