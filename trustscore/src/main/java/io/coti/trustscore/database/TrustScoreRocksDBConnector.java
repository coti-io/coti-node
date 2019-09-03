package io.coti.trustscore.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.model.BucketEvents;
import io.coti.trustscore.model.MerchantRollingReserveAddresses;
import io.coti.trustscore.model.TrustScores;
import org.rocksdb.RocksIterator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Primary
@Service
public class TrustScoreRocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                BucketEvents.class.getName(),
                TrustScores.class.getName(),
                MerchantRollingReserveAddresses.class.getName()
        ));
    }
    @Override
    public void init(String dbPath) {
        super.init(dbPath);

        userListDownloader();
    }

    private void userListDownloader() {
        RocksIterator iterator;

        iterator = getIterator(TrustScores.class.getName());
        iterator.seekToFirst();

        PrintWriter writer = null;
        if (iterator.isValid()){
            try {
                writer = new PrintWriter("userhashlist.txt", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        while (iterator.isValid()) {
            TrustScoreData trustScoreData = (TrustScoreData) SerializationUtils.deserialize(iterator.value());
            writer.println(trustScoreData.getHash().toHexString());
            iterator.next();
        }
        iterator.close();

        if(writer!=null) {
            writer.close();
        }
    }
}

