package io.coti.cotinode.interfaces;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

public interface ISourceSelector {
    public void setSourceMap(ISourceList sourceList);
    public ISourceList selectSources(int trustScore, ZonedDateTime timestamp, int minSourcePercentage, int totalSourceNum, int maxNeighbourhoodRadius);
    public ISourceList  chooseWeightedByTimestamp(ISourceList sourceList, ZonedDateTime timestamp);
    public void attachToSource(ITransaction newTransaction, ITransaction source);
    public void delete(ITransaction transaction);
}
