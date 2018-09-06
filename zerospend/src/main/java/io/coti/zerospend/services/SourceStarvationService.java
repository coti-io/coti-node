package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SourceStarvationService {
    private final long MINIMUM_WAIT_TIME_IN_SECONDS = 300;
    private final long SOURCE_STARVATION_CHECK_TASK_DELAY = 5000;

    @Autowired
    private IClusterService clusterService;
    @Autowired
    private TransactionCreationService transactionCreationService;

    @Scheduled(fixedDelay = SOURCE_STARVATION_CHECK_TASK_DELAY)
    public void checkSourcesStarvation() {
        log.info("Checking...");
        Date now = new Date();
        List<List<TransactionData>> sourceListsByTrustScore = Collections.unmodifiableList(clusterService.getSourceListsByTrustScore());

        sourceListsByTrustScore
                .stream()
                .flatMap(Collection::stream)
                .filter(transactionData -> !(transactionData.isGenesis()))
                .collect(Collectors.toList()).forEach(transactionData -> {
            double minimumWaitingTimeInMilliseconds = ((100 - transactionData.getSenderTrustScore()) * 15 + MINIMUM_WAIT_TIME_IN_SECONDS) * 1000;
            double actualWaitingTimeInMilliseconds = now.getTime() - transactionData.getAttachmentTime().getTime();
            log.info("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", transactionData.getHash(), millisecondsToMinutes(actualWaitingTimeInMilliseconds), millisecondsToMinutes(minimumWaitingTimeInMilliseconds));
            if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                transactionCreationService.createNewStarvationZeroSpendTransaction(transactionData);
            }
        });
    }

    String millisecondsToMinutes(double milliseconds) {
        return "" + (int) (milliseconds / 60000) + ":" + (int) ((milliseconds % 60000) / 1000);
    }
}