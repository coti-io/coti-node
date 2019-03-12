package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionService extends BaseNodeTransactionService {
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    protected boolean isClusterStampInProcess(){
        return clusterStampService.isClusterStampInProcess();
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        //TODO 2/21/2019 astolia: maybe should change to  clusterStampState == OFF??
        if( !clusterStampService.isReadyForClusterStamp() ) {
            dspVoteService.preparePropagatedTransactionForVoting(transactionData);
        }
    }
}