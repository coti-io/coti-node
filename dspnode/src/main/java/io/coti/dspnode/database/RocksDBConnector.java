package io.coti.dspnode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.RejectedTransactions;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
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
                UnconfirmedReceivedTransactionHashes.class.getName(),
                UnconfirmedTransactionDspVotes.class.getName(),
                RejectedTransactions.class.getName()
        ));
        resetTransactionColumnFamilyNames.addAll(Arrays.asList(
                UnconfirmedReceivedTransactionHashes.class.getName(),
                UnconfirmedTransactionDspVotes.class.getName()
        ));
    }
}
