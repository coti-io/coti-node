package io.coti.zerospend.services;

import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IClusterService;
import io.coti.zerospend.services.interfaces.IZeroSpendTrxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class StarvationService {
    @Value("${zerospend.starvation.numerator}")
    private long starvationPeriod;

    private double divisionFactor = 0.001;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IZeroSpendTrxService zeroSpendTrxService;

    @Scheduled(fixedDelayString = "${zerospend.starvation.scheduled.delay}")
    public void checkSourcesStarvation(){
            Date now = new Date();
            Vector<TransactionData>[] vectors = clusterService.getSourceListsByTrustScore();
            List<TransactionData> transactionsNeedZS = new LinkedList<>();
            for (Vector<TransactionData> vector : vectors) {
                for (TransactionData transactionData : vector) {
                    if (Double.valueOf(0).equals(transactionData.getSenderTrustScore())) {
                        log.error("Illegal trustScore ! The trust score of the transaction {} is 0.", transactionData);
                    }
                    double minWaitingTimeInMiliseconds = starvationPeriod / (transactionData.getSenderTrustScore() * divisionFactor);
                    log.info("{}",transactionData);
                    long actualWaitingTimeInMiliseconds = now.getTime() - transactionData.getAttachmentTime().getTime();
                    if (actualWaitingTimeInMiliseconds > minWaitingTimeInMiliseconds) {
                        transactionsNeedZS.add(transactionData);
                    }
                }
            }
            for(TransactionData transactionData: transactionsNeedZS){
                zeroSpendTrxService.receiveZeroSpendTransaction(transactionData);
            }
    }
}
