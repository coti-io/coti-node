package io.coti.fullnode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.fullnode.model.ExplorerIndexes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                ExplorerIndexes.class.getName(),
                AddressDataHashes.class.getName(),
                TransactionDataHashes.class.getName()
        ));
    }
}

