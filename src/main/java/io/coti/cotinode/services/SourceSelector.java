package io.coti.cotinode.services;

import io.coti.cotinode.services.interfaces.ISourceSelector;
import io.coti.cotinode.model.Interfaces.ITransaction;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@Component
public class SourceSelector implements ISourceSelector {

    @Override
    public List<ITransaction> selectSourcesForAttachment(
            Map<Integer,? extends List<? extends ITransaction>> trustScoreToTransactionMapping,
            int transactionTrustScore,
            Date transactionCreationTime,
            int minSourcePercentage,
            int maxNeighbourhoodRadius) {

        List<ITransaction> neighbourSources = getNeighbourSources(
                trustScoreToTransactionMapping,
                transactionTrustScore,
                maxNeighbourhoodRadius,
                minSourcePercentage);

        return selectTwoOptimalSources(neighbourSources, transactionCreationTime);
    }

    private List<ITransaction> getNeighbourSources(
            Map<Integer,? extends List<? extends ITransaction>> trustScoreToSourceListMapping,
            int transactionTrustScore,
            int maxTrustScoreRadius,
            int minSourcePercentage){

        List<ITransaction> neighbourSources = new Vector<>();

        AtomicInteger numberOfSources = new AtomicInteger();
        trustScoreToSourceListMapping.forEach((score, transactions) -> {
            numberOfSources.addAndGet(transactions.size());
        });

        for(int trustScoreDifference = 1; trustScoreDifference < maxTrustScoreRadius; trustScoreDifference++) {
            int lowTrustScore = transactionTrustScore - trustScoreDifference;
            int highTrustScore = transactionTrustScore + trustScoreDifference;
            if (lowTrustScore >= 1 && trustScoreToSourceListMapping.containsKey(lowTrustScore)) {
                neighbourSources.addAll(trustScoreToSourceListMapping.get(lowTrustScore));
            }
            if (highTrustScore <= 100 &&
                    trustScoreToSourceListMapping.containsKey(highTrustScore)) {
                neighbourSources.addAll(trustScoreToSourceListMapping.get(highTrustScore));
            }
            if (neighbourSources.size() / numberOfSources.get() > (double) minSourcePercentage / 100) {
                break;
            }
        }
        return neighbourSources;
    }

    private List<ITransaction> selectTwoOptimalSources(
            List<ITransaction> transactions,
            Date transactionCreationTime) {
        List<ITransaction> olderSources =
                transactions.stream().
                        filter(s -> s.getCreateDateTime().before(transactionCreationTime)).collect(toList());

        if(olderSources.size() <= 2) {
            return olderSources;
        }

        // Calculate total timestamp differences from the transaction's timestamp
        long totalWeight =
                olderSources.stream().
                        map(s -> transactionCreationTime.getTime() - s.getCreateDateTime().getTime()).mapToLong(Long::longValue).sum();

        // Now choose sources, randomly weighted by timestamp difference ("older" transactions have a bigger chance to be selected)
        List<ITransaction> randomWeightedSources = new Vector<ITransaction>();
        while(randomWeightedSources.size() < 2) {

            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < olderSources.size(); ++i) {
                random -=  transactionCreationTime.getTime()- olderSources.get(i).getCreateDateTime().getTime();
                if (random < 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            ITransaction randomSource = olderSources.get(randomIndex);

            if(randomWeightedSources.size() == 0)
                randomWeightedSources.add(randomSource);
            else if(randomWeightedSources.size() == 1 && randomSource != randomWeightedSources.iterator().next())
                randomWeightedSources.add(randomSource);
        }

        //logger.debug("Chose randomly weighted sources:\n" + randomWeightedSources);

        return olderSources;

    }

}
