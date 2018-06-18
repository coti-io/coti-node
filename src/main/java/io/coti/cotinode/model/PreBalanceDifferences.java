package io.coti.cotinode.model;

import io.coti.cotinode.data.UnconfirmedTransactionData;
import lombok.Data;

@Data
public class PreBalanceDifferences extends Collection<UnconfirmedTransactionData> {

    public PreBalanceDifferences() {
    }

    public void init() {
        super.init();
    }
}