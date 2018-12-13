package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.FinancialServerTransactionData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FinancialServerTransactions extends Collection<FinancialServerTransactionData> {

    public FinancialServerTransactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
