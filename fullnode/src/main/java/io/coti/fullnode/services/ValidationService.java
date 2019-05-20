package io.coti.fullnode.services;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService extends BaseNodeValidationService {
    @Autowired
    private FeeService feeService;

    public boolean validateFullNodeFeeDataIntegrity(TransactionData transactionData) {
        FullNodeFeeData fullNodeFeeData = transactionData.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof FullNodeFeeData).map(FullNodeFeeData.class::cast).findFirst().get();
        return feeService.validateFeeData(fullNodeFeeData);
    }
}
