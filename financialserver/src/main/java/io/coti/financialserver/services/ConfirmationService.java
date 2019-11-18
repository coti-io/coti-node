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
    @Autowired
    private MintingService mintingService;

    @Override
    protected void continueHandleConfirmedTransaction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.TokenGeneration) {
            currencyService.addToPendingCurrencyTransactionQueue(transactionData);
        } else if (transactionData.getType() == TransactionType.TokenMinting) {
            mintingService.addToConfirmedTokenMintingFeeTransactionQueue(transactionData);
        }
    }

}
