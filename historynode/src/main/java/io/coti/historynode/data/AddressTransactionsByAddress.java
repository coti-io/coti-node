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
    private HashMap<Instant, HashSet<Hash>> transactionHashes;


    @Override
    public Hash getHash() {
        return address;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
