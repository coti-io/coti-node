package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class SourceSelector implements ISourceSelector {

    @Value("${min.source.percentage}")
    private int minSourcePercentage;
    @Value("${max.neighbourhood.radius}")
    private int maxNeighbourhoodRadius;

    @Override
    public List<TransactionData> selectSourcesForAttachment(
            List<Set<Hash>> trustScoreToTransactionMapping,
            Map<Hash, TransactionData> sourceMap, double transactionTrustScore, ReentrantReadWriteLock readWriteLock) {

        List<TransactionData> neighbourSources = getNeighbourSources(
                trustScoreToTransactionMapping,
                sourceMap,
                transactionTrustScore, readWriteLock);

        return selectTwoOptimalSources(neighbourSources);
    }

    private List<TransactionData> getNeighbourSources(
            List<Set<Hash>> trustScoreToSourceListMapping,
            Map<Hash, TransactionData> sourceMap, double transactionTrustScore, ReentrantReadWriteLock readWriteLock) {

        List<TransactionData> neighbourSources = new LinkedList<>();
        try {
            readWriteLock.readLock().lock();
            int roundedTrustScore = (int) Math.round(transactionTrustScore);
            int numberOfSources = getNumberOfSources(trustScoreToSourceListMapping);
            if (numberOfSources > 0) {
                int lowIndex = roundedTrustScore - 1;
                int highIndex = roundedTrustScore + 1;

                neighbourSources.addAll(trustScoreToSourceListMapping.get(roundedTrustScore).stream().map(hash -> SerializationUtils.clone(sourceMap.get(hash))).collect(toList()));

                for (int trustScoreDifference = 0; trustScoreDifference < maxNeighbourhoodRadius; trustScoreDifference++) {
                    if (lowIndex >= 0) {
                        neighbourSources.addAll(trustScoreToSourceListMapping.get(lowIndex).stream().map(hash -> SerializationUtils.clone(sourceMap.get(hash))).collect(toList()));
                    }
                    if (highIndex <= 100) {
                        neighbourSources.addAll(trustScoreToSourceListMapping.get(highIndex).stream().map(hash -> SerializationUtils.clone(sourceMap.get(hash))).collect(toList()));
                    }
                    if ((double) neighbourSources.size() / numberOfSources > (double) minSourcePercentage / 100) {
                        break;
                    }
                    lowIndex--;
                    highIndex++;
                }
            }
            return neighbourSources;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private int getNumberOfSources(List<Set<Hash>> trustScoreToSourceListMapping) {
        int numberOfSources = 0;
        for (Set<Hash> hashes : trustScoreToSourceListMapping) {
            if (hashes != null) {
                numberOfSources += hashes.size();
            }
        }
        return numberOfSources;
    }

    private List<TransactionData> selectTwoOptimalSources(List<TransactionData> transactions) {

        Instant now = Instant.now();
        List<TransactionData> olderSources =
                transactions.stream().
                        filter(s -> !s.getAttachmentTime().isAfter(now)).collect(toList());

        if (olderSources.size() <= 2) {
            return olderSources;
        }

        // Calculate total timestamp differences from the transaction's timestamp
        long totalWeight =
                olderSources.stream().
                        map(s -> Duration.between(s.getAttachmentTime(), now).toMillis()).mapToLong(Long::longValue).sum();

        // Now choose sources, randomly weighted by timestamp difference ("older" transactions have a bigger chance to be selected)
        List<TransactionData> randomWeightedSources = new LinkedList<>();
        while (randomWeightedSources.size() < 2) {

            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < olderSources.size(); ++i) {
                random -= Duration.between(olderSources.get(i).getAttachmentTime(), now).toMillis();
                if (random < 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            TransactionData randomSource = olderSources.get(randomIndex);

            if (randomWeightedSources.isEmpty() || (randomWeightedSources.size() == 1 && randomSource != randomWeightedSources.iterator().next())) {
                randomWeightedSources.add(randomSource);
            }
        }

        return randomWeightedSources;

    }

}
