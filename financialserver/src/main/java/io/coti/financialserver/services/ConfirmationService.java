package io.coti.financialserver.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    private CurrencyService currencyService;

    @Override
    protected void continueHandleTCCConfirmedTransaction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.TokenGeneration) {
            currencyService.handleTCCConfirmedTransaction(transactionData);
        }
    }
}
