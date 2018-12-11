package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;

@Service
public class ReceiverBaseTransactionOwners extends Collection<ReceiverBaseTransactionOwnerData> {

    public ReceiverBaseTransactionOwners() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
