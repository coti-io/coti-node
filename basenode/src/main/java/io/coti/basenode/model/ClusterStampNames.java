package io.coti.basenode.model;

import io.coti.basenode.data.ClusterStampNameData;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ClusterStampNames extends Collection<ClusterStampNameData> {

    public void init() {
        super.init();
    }

    public Optional<String> getMajorAndSetTokens(List<String> tokens){
        Optional<String> majorName = Optional.empty();
        RocksIterator iterator = getIterator();
        iterator.seekToFirst();
        while (iterator.isValid()) {
            ClusterStampNameData deserialized = (ClusterStampNameData) SerializationUtils.deserialize(iterator.value());
            if (deserialized.getName().startsWith("clusterstamp_m")) {
                majorName = Optional.of(deserialized.getName());
            }
            else{
                tokens.add(deserialized.getName());
            }
            iterator.next();
        }
        return majorName;


    }

}
