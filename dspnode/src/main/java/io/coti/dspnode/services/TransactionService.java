package io.coti.dspnode.services;

import io.coti.common.data.TransactionData;
import io.coti.common.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    private TransactionHelper transactionHelper;

    public void attachUnconfirmedPropagatedTransaction(TransactionData transactionData) {
        if (!transactionHelper.validateAddresses(
                transactionData.getBaseTransactions(),
                transactionData.getHash(),
                transactionData.getTransactionDescription(),
                transactionData.getSenderTrustScore(),
                transactionData.getCreateTime())) {
            log.info("Unable to validate addresses of propagated transaction!!!");
            return;
       }

       if(!transactionHelper.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())){
           log.info("Pre balance check failed!!!");
            return;
       }

    }
}
