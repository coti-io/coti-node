package io.coti.financialserver.jobs;

import io.coti.financialserver.data.*;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.services.DisputeService;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.time.Instant;

@Component
public class DisputeJob {

    private static final int AUTO_REJECT_AFTER_HOURS = 24;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private Disputes disputes;

    @Scheduled(fixedDelay = 60000)
    public void autoRejectByMerchantDisputeItems() {

        DisputeData disputeData;

        RocksIterator iterator = disputes.databaseConnector.getIterator(disputes.getClass().getName());
        iterator.seekToFirst();

        while (iterator.isValid()) {

            disputeData = (DisputeData) SerializationUtils.deserialize(iterator.value());
            iterator.next();

            if(!disputeData.getDisputeStatus().equals(DisputeStatus.Recall) ||
                    Instant.now().isBefore(disputeData.getCreationTime().plusSeconds(AUTO_REJECT_AFTER_HOURS*60*60))) {
                continue;
            }

            disputeData.getDisputeItems().forEach(disputeItemData -> {
                if(disputeItemData.getStatus().equals(DisputeItemStatus.Recall)) {
                    disputeItemData.setStatus(DisputeItemStatus.RejectedByMerchant);
                }
            });

            disputeData.setDisputeStatus(DisputeStatus.Claim);
            disputeService.update(disputeData);
        }
    }
}
