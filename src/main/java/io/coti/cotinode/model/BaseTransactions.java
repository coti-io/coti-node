package io.coti.cotinode.model;

import io.coti.cotinode.data.AddressData;
import io.coti.cotinode.data.BaseTransactionData;

public class BaseTransactions extends Collection<BaseTransactionData> {

    public BaseTransactions() {
        init();
        dataObjectClass = BaseTransactionData.class;
    }

    public void init() {
        super.init();
    }
}
