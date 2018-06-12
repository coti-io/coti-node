package io.coti.cotinode.interfaces;

import io.coti.cotinode.model.Interfaces.ITransaction;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ISourceSelector {
    public List<ITransaction> selectSourcesForAttachment(
            Map<Integer, List<ITransaction>> trustScoreToTransactionMapping,
            int transactionTrustScore,
            Date transactionCreationTime,
            int minSourcePercentage,
            int totalSourceNum,
            int maxNeighbourhoodRadius);
}
