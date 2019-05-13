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

}
