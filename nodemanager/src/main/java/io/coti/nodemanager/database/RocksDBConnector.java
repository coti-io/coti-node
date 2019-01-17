package io.coti.nodemanager.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.model.NodeHistory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Primary
public class RocksDBConnector extends BaseNodeRocksDBConnector {


    @Override
    protected void setColumnFamily() {
        columnFamilyClassNames = Arrays.asList(
                "DefaultColumnClassName",
                ActiveNode.class.getName(),
                NodeHistory.class.getName());
    }
}
