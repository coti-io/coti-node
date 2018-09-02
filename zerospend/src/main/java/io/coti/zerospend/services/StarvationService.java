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
public class StarvationService {
    private final long DIVISION_FACTOR = 10000;
    private final long SOURCE_STARVATION_CHECK_TASK_DELAY = 5000;

    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ZeroSpendTransactionCreationService zeroSpendTransactionCreationService;

    @Scheduled(fixedDelay = SOURCE_STARVATION_CHECK_TASK_DELAY)
    public void checkSourcesStarvation() {
        log.info("Checking...");
        Date now = new Date();
        List<List<TransactionData>> sourceListsByTrustScore = Collections.unmodifiableList(clusterService.getSourceListsByTrustScore());

        sourceListsByTrustScore
                .stream()
                .flatMap(Collection::stream)
                .filter(transactionData -> !transactionData.isGenesis())
                .collect(Collectors.toList()).forEach(transactionData -> {
            double minimumWaitingTimeInSeconds = DIVISION_FACTOR / transactionData.getSenderTrustScore();
            double minimumWaitingTimeInMilliseconds = minimumWaitingTimeInSeconds * 1000;
            long actualWaitingTimeInMilliseconds = now.getTime() - transactionData.getAttachmentTime().getTime();
            log.info("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", transactionData.getHash(), actualWaitingTimeInMilliseconds, minimumWaitingTimeInMilliseconds);
            if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                zeroSpendTransactionCreationService.createNewStarvationZeroSpendTransaction(transactionData);
            }
        });
    }
}