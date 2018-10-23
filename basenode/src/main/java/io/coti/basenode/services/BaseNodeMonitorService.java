package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeMonitorService implements IMonitorService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ITransactionService transactionService;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void lastState() {
        log.info("Transactions = {}, TccConfirmed = {}, DspConfirmed = {}, Confirmed = {}, LastIndex = {}, Sources = {}, PostponedTransactions = {}",
                transactionHelper.getTotalTransactions(),
                confirmationService.getTccConfirmed(),
                confirmationService.getDspConfirmed(),
                confirmationService.getTotalConfirmed(),
                transactionIndexService.getLastTransactionIndexData().getIndex(),
                clusterService.getTotalSources(),
                transactionService.totalPostponedTransactions());
    }
}
