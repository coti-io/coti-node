package io.coti.storagenode.data.enums;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.Transactions;

public enum ElasticSearchData {

    ADDRESSES(Addresses.class.getSimpleName().toLowerCase(), AddressData.class.getSimpleName()),
    TRANSACTIONS(Transactions.class.getSimpleName().toLowerCase(), TransactionData.class.getSimpleName());

    private String index;
    private String object;

    ElasticSearchData(String index, String object) {
        this.index = index;
        this.object = object;
    }

    public String getIndex() {
        return index;
    }

    public String getObjectName() {
        return object;
    }
}
