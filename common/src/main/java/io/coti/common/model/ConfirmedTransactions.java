package io.coti.common.model;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.TransactionData;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Slf4j
@Service
public class ConfirmedTransactions extends Collection<ConfirmationData> {

    private String columnFamilyName = getClass().getName();

    @Autowired
    private IDatabaseConnector databaseConnector;

    public ConfirmedTransactions() {
    }

    public void init() {
        super.init();
    }

    @Override
    public void put(IEntity entity) {
        try {
            databaseConnector.put(columnFamilyName, entity.getKey().getBytes(), SerializationUtils.serialize(entity));
            ConfirmationData confirmationData = (ConfirmationData) entity;
            TransactionData transactionData = confirmationData.getTransactionData();//confirmationData.get

            for (BaseTransactionData baseTransaction : transactionData.getBaseTransactions()) {
                if (!confirmationData.getAddressHashToValueTransferredMapping().containsKey(baseTransaction.getKey())) {
                    log.warn("Warning! The confirmationData holds an address that does not exist in the transaction it " +
                            "points to ");
                    return;
                }
                baseTransaction.setAmount(confirmationData.getAddressHashToValueTransferredMapping()
                        .get(baseTransaction.getKey()));

            }
            transactionData.setCreateTime(confirmationData.getCreationTIme());
            transactionData.setDspConsensus(confirmationData.isDoubleSpendPreventionConsensus());
            transactionData.setTrustChainConsensus(confirmationData.isTrustChainConsensus());
            databaseConnector.put(TransactionData.class.getName(), entity.getKey().getBytes(),
                    SerializationUtils.serialize(transactionData));

        } catch (Exception ex) {
            log.error("Exception while inserting data to confimationTable and transactionTable");
        }


    }
}
