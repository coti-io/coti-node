package io.coti.cotinode.model;

import io.coti.cotinode.data.UnconfirmedTransactionData;
import lombok.Data;

@Data
public class UnconfirmedTransaction extends Collection<UnconfirmedTransactionData> {

    public UnconfirmedTransaction() {
    }

    public void init() {
        super.init();
    }
}