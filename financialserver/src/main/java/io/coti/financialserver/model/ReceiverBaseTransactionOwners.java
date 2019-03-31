package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import org.springframework.stereotype.Service;

@Service
public class ReceiverBaseTransactionOwners extends Collection<ReceiverBaseTransactionOwnerData> {

    public ReceiverBaseTransactionOwners() {
    }

    public void init() {
        super.init();
    }
}
