package io.coti.cotinode.model;

import io.coti.cotinode.data.ConfirmationData;
import lombok.Data;

@Data
public class UnconfirmedTransactions extends Collection<ConfirmationData> {

    public UnconfirmedTransactions() {
    }

    public void init() {
        super.init();
    }
}