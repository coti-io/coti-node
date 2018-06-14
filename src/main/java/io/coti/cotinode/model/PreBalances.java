package io.coti.cotinode.model;

import io.coti.cotinode.data.AddressData;
import io.coti.cotinode.data.PreBalance;

public class PreBalances extends Collection<PreBalance> {

    public PreBalances() {
        init();
        dataObjectClass = PreBalance.class;
    }

    public void init() {
        super.init();
    }
}
