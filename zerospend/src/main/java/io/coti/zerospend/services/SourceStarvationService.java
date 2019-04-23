package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SourceStarvationService {
    private final long MINIMUM_WAIT_TIME_IN_SECONDS = 300;
    private final long SOURCE_STARVATION_CHECK_TASK_DELAY = 5000;

    @Autowired
    private IClusterService clusterService;
    @Autowired
    private TransactionCreationService transactionCreationService;

    @Scheduled(fixedDelay = SOURCE_STARVATION_CHECK_TASK_DELAY)
    public void checkSourcesStarvation() {
        log.debug("Checking Source Starvation");
        Instant now = Instant.now();
        List<List<TransactionData>> sourceListsByTrustScore = Collections.unmodifiableList(clusterService.getSourceListsByTrustScore());

        sourceListsByTrustScore
                .stream()
                .flatMap(Collection::stream)
                .filter(transactionData -> !(transactionData.isGenesis()))
                .collect(Collectors.toList()).forEach(transactionData -> {
            long minimumWaitingTimeInMilliseconds = (long) ((100 - transactionData.getSenderTrustScore()) * 15 + MINIMUM_WAIT_TIME_IN_SECONDS) * 10000;
            long actualWaitingTimeInMilliseconds = Duration.between(transactionData.getAttachmentTime(), now).toMillis();
            log.debug("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", transactionData.getHash(), millisecondsToMinutes(actualWaitingTimeInMilliseconds), millisecondsToMinutes(minimumWaitingTimeInMilliseconds));
            if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                transactionCreationService.createNewStarvationZeroSpendTransaction(transactionData);
            }
        });
    }

    String millisecondsToMinutes(long milliseconds) {
        return new SimpleDateFormat("mm:ss").format(new Date(milliseconds));
    }
}