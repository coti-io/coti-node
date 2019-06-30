package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedMap;

@Data
public class AddressTransactionsByAddress implements IEntity {

    private Hash address;
//    private HashMap<Hash, HashSet<Hash>> transactionHashesByDates;
    private SortedMap<LocalDate, HashSet<Hash>> transactionHashesByDates;


    public AddressTransactionsByAddress(Hash address, SortedMap<LocalDate, HashSet<Hash>> transactionHashesByDate) {
        this.address = address;
        this.transactionHashesByDates = transactionHashesByDate;
    }

    @Override
    public Hash getHash() {
        return address;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
