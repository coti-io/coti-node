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


    public void init() {
        super.init();
    }
}