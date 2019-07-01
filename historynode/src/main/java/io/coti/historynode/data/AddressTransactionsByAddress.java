package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;

@Data
public class AddressTransactionsByAddress implements IEntity {

    private Hash address;
    private HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates;
//    private SortedMap<LocalDate, HashSet<Hash>> transactionHashesByDates;


    public AddressTransactionsByAddress(Hash address, HashMap<LocalDate, HashSet<Hash>> transactionHashesByDate) {
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
