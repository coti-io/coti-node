package io.coti.common.model;

import io.coti.common.data.ConfirmationData;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnconfirmedTransactions extends Collection<ConfirmationData> {

    public UnconfirmedTransactions() {
    }

                " please use 'public void putConfirmationDataAndUpdateTransaction(IEntity entity)'");




    public void putConfirmationDataAndUpdateTransaction(IEntity entity){
        modelUtil.putConfirmedOrUnconfirmedHelper(columnFamilyName,entity);
    }

    public void init() {
        super.init();
    }
}