package io.coti.zero_spend.data;

import io.coti.common.data.Hash;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
public class TransactionIndexData implements IEntity {

    private Hash key;
    private long index;

    private TransactionIndexData() {
    }

    public TransactionIndexData(Hash key, long index) {
        this.key = key;
        this.index = index;
    }

}
