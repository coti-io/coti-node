package io.coti.dspnode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.coti.common.communication.interfaces.ITransactionPropagationPublisher;
import io.coti.common.communication.interfaces.ITransactionReceiver;
import io.coti.common.data.TransactionData;
import io.coti.common.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private ITransactionReceiver transactionReceiver;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private ITransactionPropagationPublisher transactionPropagator;

    @PostConstruct
    private void init(){
        transactionReceiver.init(unconfirmedTransactionHandler);
    }

    private Consumer<TransactionData> unconfirmedTransactionHandler = transactionData -> {
        try {
            attachUnconfirmedPropagatedTransaction(transactionData);
            transactionPropagator.propagateTransaction(transactionData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    };

    public void attachUnconfirmedPropagatedTransaction(TransactionData transactionData) throws JsonProcessingException {
        log.info("attaching transaction: {}", transactionData.getHash().toHexString());
        if (!transactionHelper.validateAddresses(
                transactionData.getBaseTransactions(),
                transactionData.getHash(),
                transactionData.getTransactionDescription(),
                transactionData.getSenderTrustScore(),
                transactionData.getCreateTime())) {
            log.info("Unable to validate addresses of propagated transaction!!!");
            return;
       }
       if(transactionHelper.isTransactionExists(transactionData.getHash())){
            log.info("Transaction already exists in database");
       }

       if(!transactionHelper.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())){
           log.info("Pre balance check failed!!!");
            return;
       }
    }
}
