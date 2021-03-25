package io.coti.fullnode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.*;
import io.coti.fullnode.model.DateAddressTransactionsHistories;
import io.coti.fullnode.model.ExplorerIndexes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                ExplorerIndexes.class.getName(),
                RequestedAddressHashes.class.getName(),
                UnconfirmedReceivedTransactionHashes.class.getName(),
                DateAddressTransactionsHistories.class.getName()
        ));
        resetTransactionColumnFamilyNames.addAll(Arrays.asList(
                DateAddressTransactionsHistories.class.getName()
        ));
    }
}
