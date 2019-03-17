package io.coti.basenode.model;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionDataHash;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TransactionDataHashes extends Collection<TransactionDataHash> {

    public TransactionDataHashes(){
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
    }

    public Set<TransactionDataHash> getHashes(){
        Set<TransactionDataHash> hashes = new HashSet<>();
        databaseConnector.isEmpty(columnFamilyName);
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        if(iterator == null){
            return Collections.emptySet();
        }
        iterator.seekToFirst();
        while (iterator.isValid()) {
            TransactionDataHash transactionDataHash = (TransactionDataHash) SerializationUtils.deserialize(iterator.value());
            transactionDataHash.setHash(new Hash(iterator.key()));
            hashes.add(transactionDataHash);
            iterator.next();
        }
        return hashes;
    }

}
