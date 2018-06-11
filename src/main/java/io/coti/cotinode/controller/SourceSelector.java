package io.coti.cotinode.controller;


import io.coti.cotinode.interfaces.ISourceList;
import io.coti.cotinode.interfaces.ISourceSelector;
import io.coti.cotinode.interfaces.ITransaction;

import io.coti.cotinode.model.SourceList;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Component
public class SourceSelector implements ISourceSelector {

    private ConcurrentHashMap<String, List<ITransaction>> sourceMap;

    @Override
    public void SetSourceMap(ISourceList sourceList) {
        sourceMap = new ConcurrentHashMap<>();
        for (ITransaction t : sourceList.getSources()) {
            if (sourceMap.containsKey(t.getMyWeight())) {
                sourceMap.put(t.getTransactionHash(), new Vector<ITransaction>());
            }
            sourceMap.get(t.getMyWeight()).add(t);
        }
    }

    @Override
    public ISourceList selectSources(int trustScore, ZonedDateTime timestamp, int minSourcePercentage, int totalSourceNum, int maxNeighbourhoodRadius) {
        // Calculate the neighbourhood radius, minimal radius is 1 (always look at neighbours)
        // Start by taking the sources with the same trust score (clone)
        ISourceList sourceList = new SourceList();
        sourceList.SetSourceList(sourceMap.get(trustScore));

        // Calculate the neighbourhood radius, minimal radius is 1 (always look at neighbours)
        for(int nr=1; nr < maxNeighbourhoodRadius; nr++) {

            if(trustScore - nr >= 1) {
                if (sourceMap.containsKey(trustScore - nr)) {
                    sourceList.addAll(sourceMap.get(trustScore - nr));
                }
            }
            if(trustScore + nr <= 100) {    // TODO replace number with const. from config file?
                if (sourceMap.containsKey(trustScore + nr)) {
                    sourceList.addAll(sourceMap.get(trustScore + nr));
                }
            }

            if((double)sourceList.size() / totalSourceNum > (double)minSourcePercentage / 100) {
                break;
            }
        }

        // Randomly choose source, weighted by timestamp difference
        return chooseWeightedByTimestamp(sourceList, timestamp);

    }

    @Override
    public ISourceList chooseWeightedByTimestamp(ISourceList sourceList, ZonedDateTime timestamp) {
        List<ITransaction> sources = sourceList.getSources().stream().filter(s -> s.getCreateDateTime().compareTo(timestamp) < 0).collect(toList());

        int sourcesNum = sources.size();

        //logger.debug("Total dt time diff:" + totalWeight + " for number of sources:" + sourcesNum);

        if(sourcesNum == 0) {
            //TODO: Verify this case is handled correctly, add unit test
            //   logger.info("No sources found to attach to");
            return new SourceList();
        }

        if(sourcesNum ==1) {
            ISourceList s = new SourceList();
            //  logger.info("Only one source found - attaching to it");
            s.add(sources.get(0));
            return s;
        }

        // Calculate total timestamp differences from the transaction's timestamp
        long totalWeight = sources.stream().map(s -> Duration.between(timestamp ,s.getCreateDateTime()).toMillis()).mapToLong(Long::longValue).sum();

        // Now choose sources, randomly weighted by timestamp difference ("older" transactions have a bigger chance to be selected)
        ISourceList randomWeightedSources = new SourceList();
        while(randomWeightedSources.size() < 2) {

            int randomIndex = -1;
            double random = Math.random() * totalWeight;
            for (int i = 0; i < sources.size(); ++i) {
                random -=  Duration.between(timestamp, sources.get(i).getCreateDateTime()).toMillis();
                if (random < 0.0d) {
                    randomIndex = i;
                    break;
                }
            }

            ITransaction randomSource = sources.get(randomIndex);

            if(randomWeightedSources.size() == 0)
                randomWeightedSources.add(randomSource);
            else if(randomWeightedSources.size() == 1 && randomSource != randomWeightedSources.getSources().iterator().next())
                randomWeightedSources.add(randomSource);
        }

        //logger.debug("Chose randomly weighted sources:\n" + randomWeightedSources);

        return randomWeightedSources;
    }

    @Override
    public void attachToSource(ITransaction newTransaction, ITransaction source) {
        List<ITransaction> transactions  = sourceMap.get(source.getMyWeight());

        if(!transactions.contains(source)) {
            //logger.error("Cannot find source:" + source);
            //throw new RuntimeException("Cannot find source:" + source);
        }
        else {
            if (newTransaction.getLeftParent() == null){
                newTransaction.setLeftParent(source);
            }
            else {
                newTransaction.setRightParent(source);
            }

        }

    }
}
