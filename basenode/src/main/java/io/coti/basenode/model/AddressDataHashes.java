package io.coti.basenode.model;

import io.coti.basenode.data.AddressDataHash;
import io.coti.basenode.data.Hash;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Service
public class AddressDataHashes extends Collection<AddressDataHash> {

    public AddressDataHashes(){
        // Empty constructor for serialization
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }

    public Set<AddressDataHash> getHashes(){
        Set<AddressDataHash> hashes = new HashSet<>();
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        iterator.seekToFirst();
        while (iterator.isValid()) {
            AddressDataHash addressDataHash = (AddressDataHash) SerializationUtils.deserialize(iterator.value());
            addressDataHash.setHash(new Hash(iterator.key()));
            hashes.add(addressDataHash);
            iterator.next();
        }
        return hashes;
    }

}
