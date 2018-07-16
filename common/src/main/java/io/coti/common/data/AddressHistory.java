package io.coti.common.data;


import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AddressTransactionsHistory implements IEntity {

    private transient Hash hash;
    private Date creationTime;
    private List<Hash> basicTransaction;


    public AddressTransactionsHistory(Hash hash) {
        this.hash = hash;
        creationTime = new Date();
        basicTransaction = new ArrayList<>();
    }

    public void addBasicTransactionHashToHistory(Hash basicTransactionHash){
        basicTransaction.add(basicTransactionHash);
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddressTransactionsHistory)) {
            return false;
        }
        return hash.equals(((AddressTransactionsHistory) other).hash);
    }
}



