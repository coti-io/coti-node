package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@Slf4j
public class TransactionService extends BaseNodeTransactionService {
    @Autowired
    private DspVoteService dspVoteService;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        if (EnumSet.of(TransactionType.Initial).contains(transactionData.getType())) {
            dspVoteService.publishDecision(transactionData.getHash());
        }
        dspVoteService.preparePropagatedTransactionForVoting(transactionData);
    }
}