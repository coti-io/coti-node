package io.coti.nodemanager.database;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.nodemanager.model.NodeHistory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Primary
public class NodeManagerRocksDBConnector extends RocksDBConnector {


    @Override
    public void setColumnFamily() {
        columnFamilyClassNames = Arrays.asList(
                "DefaultColumnClassName",
                NodeHistory.class.getName());
    }


}
