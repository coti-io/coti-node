package io.coti.zerospend.services;

import io.coti.common.data.TransactionData;
import io.coti.common.services.BaseNodeTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionService extends BaseNodeTransactionService {
    @Autowired
    private DspVoteService dspVoteService;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        dspVoteService.preparePropagatedTransactionForVoting(transactionData);
    }
}