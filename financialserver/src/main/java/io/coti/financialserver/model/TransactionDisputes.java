package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.TransactionDisputesData;
import org.springframework.stereotype.Service;

@Service
public class TransactionDisputes extends Collection<TransactionDisputesData> {

    public TransactionDisputes() {
    }

    public void init() {
        super.init();
    }
}
