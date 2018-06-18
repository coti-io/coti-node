package io.coti.cotinode.model;

import io.coti.cotinode.data.UnconfirmedTransactionData;
import lombok.Data;

@Data
public class UnconfirmedTransactions extends Collection<UnconfirmedTransactionData> {

    public UnconfirmedTransactions() {
    }

    public void init() {
        super.init();
    }
}