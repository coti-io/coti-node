package io.coti.basenode.data;


import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AddressTransactionsHistory implements IEntity {

    private transient Hash hash;
    private Date creationTime;
    private List<Hash> transactionsHistory;


    public AddressTransactionsHistory(Hash hash) {
        this.hash = hash;
        creationTime = new Date();
        transactionsHistory = new ArrayList<>();
    }

    public void addTransactionHashToHistory(Hash transactionHash) {
        transactionsHistory.add(transactionHash);
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



