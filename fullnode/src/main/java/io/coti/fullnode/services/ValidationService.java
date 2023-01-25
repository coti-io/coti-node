package io.coti.fullnode.services;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeValidationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.coti.fullnode.services.NodeServiceManager.feeService;

@Service
@Primary
public class ValidationService extends BaseNodeValidationService {

    @Override
    public Boolean validateFullNodeFeeDataIntegrity(TransactionData transactionData) {
        Optional<FullNodeFeeData> optionalFullNodeFeeData = transactionData.getBaseTransactions().stream().filter(FullNodeFeeData.class::isInstance).map(FullNodeFeeData.class::cast).findFirst();
        return optionalFullNodeFeeData.isPresent() && feeService.validateFeeData(optionalFullNodeFeeData.get());
    }
}
