package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
public class TransactionIndexData implements IEntity {

    private transient Hash hash;
    private long index;

    private TransactionIndexData() {
    }

    public TransactionIndexData(Hash hash, long index) {
        this.hash = hash;
        this.index = index;
    }
}