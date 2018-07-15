package io.coti.common.model;


import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.TransactionData;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;


@Component
@Slf4j
public class ModelUtil {
    @Autowired
    private IDatabaseConnector databaseConnector;

    public void putConfirmedOrUnconfirmedHelper(String columnName, IEntity entity){
        try {
            databaseConnector.put(columnName, entity.getKey().getBytes(), SerializationUtils.serialize(entity));
     /*       ConfirmationData confirmationData = (ConfirmationData) entity;
            TransactionData transactionData = confirmationData.getTransactionData();//confirmationData.get

            for (BaseTransactionData baseTransaction : transactionData.getBaseTransactions()) {
                if (!confirmationData.getAddressHashToValueTransferredMapping().containsKey(baseTransaction.getAddressHash())) {
                    log.warn("Warning! The confirmationData holds an address that does not exist in the transaction it " +
                            "points to ");
                    return;
                }
                baseTransaction.setAmount(confirmationData.getAddressHashToValueTransferredMapping()
                        .get(baseTransaction.getAddressHash()));

            }
            transactionData.setCreateTime(confirmationData.getCreationTIme());
            transactionData.setDspConsensus(confirmationData.isDoubleSpendPreventionConsensus());
            transactionData.setTrustChainConsensus(confirmationData.isTrustChainConsensus());
            databaseConnector.put(Transactions.class.getName(), transactionData.getHash().getBytes(),
                    SerializationUtils.serialize(transactionData)); */

        } catch (Exception ex) {
            log.error("Exception while inserting data to confimationTable and transactionTable");
        }

    }

}
