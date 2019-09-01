package io.coti.basenode.model;

import io.coti.basenode.data.LastClusterStampVersionData;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Service
public class LastClusterStampVersions extends Collection<LastClusterStampVersionData> {

    public void init() {
        super.init();
    }

    public void replacePreviousVersion(LastClusterStampVersionData lastClusterStampVersionData) {
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            if(iterator.isValid()){
                LastClusterStampVersionData previous = (LastClusterStampVersionData)SerializationUtils.deserialize(iterator.value());
                delete(previous);
            }
            databaseConnector.put(columnFamilyName, lastClusterStampVersionData.getHash().getBytes(), SerializationUtils.serialize(lastClusterStampVersionData));
        } finally {
            iterator.close();
        }
    }

    public LastClusterStampVersionData get(){
        LastClusterStampVersionData lastClusterStampVersionData = null;
        RocksIterator iterator = databaseConnector.getIterator(columnFamilyName);
        try {
            iterator.seekToFirst();
            if (iterator.isValid()) {
                lastClusterStampVersionData = (LastClusterStampVersionData) SerializationUtils.deserialize(iterator.value());
            }
        } finally {
            iterator.close();
            return lastClusterStampVersionData;

        }
    }
}
