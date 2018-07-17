package io.coti.common.model;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.TccInfo;
import io.coti.common.data.TransactionData;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.Date;

@Slf4j
@Service
public class ConfirmedTransactions extends Collection<ConfirmationData> {
    
    @Autowired
    public IDatabaseConnector databaseConnector;

    @Autowired
    private Transactions transactions;

    public ConfirmedTransactions() {
    }

    public void init() {
        super.init();
    }

    public void putConfirmedAndUpdateTransaction(IEntity entity, TccInfo tccInfo){
        try {
            databaseConnector.put(columnFamilyName, entity.getHash().getBytes(), SerializationUtils.serialize(entity));
            ConfirmationData confirmationData = (ConfirmationData) entity;
            TransactionData transactionData = transactions.getByHash(confirmationData.getHash());

            for (BaseTransactionData baseTransaction : transactionData.getBaseTransactions()) {
                if (!confirmationData.getAddressHashToValueTransferredMapping().containsKey(baseTransaction.getAddressHash())) {
                    log.warn("Warning! The confirmationData holds an address that does not exist in the transaction it " +
                            "points to ");
                    return;
                }
                baseTransaction.setAmount(confirmationData.getAddressHashToValueTransferredMapping()
                        .get(baseTransaction.getAddressHash()));

            }
            transactionData.setDspConsensus(confirmationData.isDoubleSpendPreventionConsensus());

            transactionData.setTrustChainConsensus(true);
            transactionData.setTrustChainTransactionHashes(tccInfo.getTrustChainTransactionHashes());
            transactionData.setTrustChainTrustScore(tccInfo.getTrustChainTrustScore());

            if(confirmationData.isDoubleSpendPreventionConsensus()){
                transactionData.setTransactionConsensusUpdateTime(new Date());
            }
            databaseConnector.put(Transactions.class.getName(), transactionData.getHash().getBytes(),
                    SerializationUtils.serialize(transactionData));

        } catch (Exception ex) {
            log.error("Exception while inserting data to confimationTable and transactionTable");
        }
    }

    @Override
    public void put(IEntity entity) {
        throw new UnsupportedOperationException("This message is unused in confirmed/unconfirmed transactions." +
                " please use 'public void putConfirmedAndUpdateTransaction(IEntity entity, boolean dspc," +
                " TccInfo tccInfo)'");

    }


}
