package io.coti.fullnode.services;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidationService extends BaseNodeValidationService {
    @Autowired
    private FeeService feeService;

    public boolean validateFullNodeFeeDataIntegrity(TransactionData transactionData) {
        Optional<FullNodeFeeData> baseTransactionOpt = transactionData.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof FullNodeFeeData).map(FullNodeFeeData.class::cast).findFirst();
        if(!baseTransactionOpt.isPresent()){
            return false;
        }
        FullNodeFeeData fullNodeFeeData = baseTransactionOpt.get();
        return feeService.validateFeeData(fullNodeFeeData);
    }
}
