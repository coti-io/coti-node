package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitorService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IClusterService clusterService;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void lastState() {
        log.info("Transactions = {}, TccConfirmed = {}, DspConfirmed = {}, Confirmed = {}, LastIndex = {}, Sources = {}",
                transactionHelper.getTotalTransactions(),
                balanceService.getTccConfirmed(),
                balanceService.getDspConfirmed(),
                balanceService.getTotalConfirmed(),
                transactionIndexService.getLastTransactionIndexData().getIndex(),
                clusterService.getTotalSources());
    }
}
