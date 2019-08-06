package io.coti.fullnode.websocket.data;

import lombok.Data;

@Data
public class TotalTransactionsMessage {

    private int totalTransactions;

    public TotalTransactionsMessage(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
