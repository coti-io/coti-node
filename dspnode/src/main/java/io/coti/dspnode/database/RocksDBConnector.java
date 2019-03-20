package io.coti.dspnode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.TransactionDataHashes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Primary
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                AddressDataHashes.class.getName(),
                TransactionDataHashes.class.getName()
        ));
    }

}