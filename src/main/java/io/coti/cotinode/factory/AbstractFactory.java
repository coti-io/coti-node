package io.coti.cotinode.factory;

import io.coti.cotinode.interfaces.*;

public abstract class AbstractFactory {
    abstract ICluster getCluster(String ClusterCluster);
    abstract IClusterHandler getClusterHandler(String ClusterHandler);
    abstract ISourceSelector getSourceSelector(String SourceSelector);
    abstract ISourceList getSourceList(String SourceList);
    abstract ITransaction getTransaction(String Transaction);
}
