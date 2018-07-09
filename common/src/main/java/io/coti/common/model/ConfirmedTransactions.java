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


    @Autowired
    private ModelUtil modelUtil;

    public ConfirmedTransactions() {
    }

    public void init() {
        super.init();
    }

    @Override
    public void put(IEntity entity) {
        modelUtil.putConfirmedOrUnconfirmedHelper(columnFamilyName,entity);

    }
}
