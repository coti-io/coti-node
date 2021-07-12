package io.coti.dspnode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Collections.singletonList(
                UnconfirmedReceivedTransactionHashes.class.getName()
        ));
        resetTransactionColumnFamilyNames.addAll(Collections.singletonList(
                UnconfirmedReceivedTransactionHashes.class.getName()
        ));
    }
}
