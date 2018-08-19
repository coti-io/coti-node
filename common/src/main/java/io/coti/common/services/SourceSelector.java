package io.coti.common.services;

import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Vector;

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
            Vector<TransactionData>[] trustScoreToTransactionMapping,
            double transactionTrustScore) {

        List<TransactionData> neighbourSources = getNeighbourSources(
                trustScoreToTransactionMapping,
                transactionTrustScore);

        return selectTwoOptimalSources(neighbourSources);
    }

    private List<TransactionData> getNeighbourSources(
            Vector<TransactionData>[] trustScoreToSourceListMapping,
            double transactionTrustScore) {

        int roundedTrustScore = (int) Math.round(transactionTrustScore);
        int numberOfSources = getNumberOfSources(trustScoreToSourceListMapping);
        int lowIndex = roundedTrustScore - 1;
        int highIndex = roundedTrustScore + 1;
        Vector<TransactionData> neighbourSources = trustScoreToSourceListMapping[roundedTrustScore];

        for (int trustScoreDifference = 0; trustScoreDifference < maxNeighbourhoodRadius; trustScoreDifference++) {
            if (lowIndex >= 0) {
                neighbourSources.addAll(trustScoreToSourceListMapping[lowIndex]);
            }
            if (highIndex <= 100) {
                neighbourSources.addAll(trustScoreToSourceListMapping[highIndex]);
            }
            if ((double) neighbourSources.size() / numberOfSources > (double) minSourcePercentage / 100) {
                break;
            }
            lowIndex--;
            highIndex++;
        }
        return neighbourSources;
    }

    private int getNumberOfSources(Vector<TransactionData>[] trustScoreToSourceListMapping) {
        int numberOfSources = 0;
        for (int i = 0; i < trustScoreToSourceListMapping.length; i++) {
            if (trustScoreToSourceListMapping[i] != null) {
                numberOfSources += trustScoreToSourceListMapping[i].size();
            }
        }
        return numberOfSources;
    }

    public List<TransactionData> selectTwoOptimalSources(
            List<TransactionData> transactions) {

        Date now = new Date();
        List<TransactionData> olderSources =
                transactions.stream().
                        filter(s -> !s.getAttachmentTime().after(now)).collect(toList());

        if (olderSources.size() <= 2) {
            return olderSources;
        }

        // Calculate total timestamp differences from the transaction's timestamp
        long totalWeight =
                olderSources.stream().
                        map(s -> now.getTime() - s.getAttachmentTime().getTime()).mapToLong(Long::longValue).sum();

        // Now choose sources, randomly weighted by timestamp difference ("older" transactions have a bigger chance to be selected)
        List<TransactionData> randomWeightedSources = new Vector<>();
        while (randomWeightedSources.size() < 2) {

            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < olderSources.size(); ++i) {
                random -= now.getTime() - olderSources.get(i).getAttachmentTime().getTime();
                if (random < 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            //log.info("in sourceSelect process randomIndex: {}: whe have source: {}??", randomIndex);
            TransactionData randomSource = olderSources.get(randomIndex);


            if (randomWeightedSources.size() == 0)
                randomWeightedSources.add(randomSource);
            else if (randomWeightedSources.size() == 1 && randomSource != randomWeightedSources.iterator().next())
                randomWeightedSources.add(randomSource);
        }

        //logger.debug("Chose randomly weighted sources:\n" + randomWeightedSources);

        return randomWeightedSources;

    }

}
