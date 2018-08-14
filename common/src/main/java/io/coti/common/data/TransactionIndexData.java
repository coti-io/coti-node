package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.math.BigInteger;

@Data
public class TransactionIndexData implements IEntity {

    private transient Hash hash;
    private BigInteger index;

    private TransactionIndexData() {
    }

    public TransactionIndexData(Hash hash, BigInteger index) {
        this.hash = hash;
        this.index = index;
    }





}
