package io.coti.fullnode.services;

import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Autowired
    private ITransactionPropagationCheckService transactionPropagationCheckService;

    @Override
    protected void restartTransactionProcessing() {
        transactionPropagationCheckService.endResendingPause();
    }
}
