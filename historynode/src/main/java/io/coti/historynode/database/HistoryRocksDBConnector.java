package io.coti.historynode.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Primary
@Service
public class HistoryRocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    public void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                AddressTransactionsByDatesHistories.class.getName(),
                AddressTransactionsByAddresses.class.getName(),
                AddressTransactionsByDates.class.getName()
        ));
    }
}
