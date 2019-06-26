package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

@Data
public class AddressTransactionsByAddress implements IEntity {

    private Hash address;
    private HashMap<Hash, HashSet<Hash>> transactionHashesByDates;

    public AddressTransactionsByAddress(Hash address, HashMap<Hash, HashSet<Hash>> transactionHashesByDates) {
        this.address = address;
        this.transactionHashesByDates = transactionHashesByDates;
    }

    @Override
    public Hash getHash() {
        return address;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
