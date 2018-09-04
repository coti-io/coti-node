package io.coti.basenode.services;

import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.IValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ValidationService implements IValidationService {
    @Autowired
    private Transactions transactions;

    @Autowired
    private IPotService potService;

    @Override
    public boolean validateBaseTransaction(BaseTransactionData baseTransactionData, Hash transactionHash) {
        BaseTransactionCryptoWrapper baseTransactionCrypto = new BaseTransactionCryptoWrapper(baseTransactionData);
        return baseTransactionCrypto.IsBaseTransactionValid(transactionHash);
    }

    @Override
    public boolean validateSource(Hash transactionHash) {
        if (transactionHash != null) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                transactionData.setValid(true);
                transactions.put(transactionData);
            }
        }
        return true;
    }

    @Override
    public boolean validateAddress(Hash address) {
        return CryptoHelper.IsAddressValid(address);
    }

    @Override
    public boolean partialValidation(TransactionData transactionData) {
        return true;
    }

    @Override
    public boolean fullValidation(TransactionData transactionData) {
        return true;
    }

    @Override
    public boolean validatePot(TransactionData transactionData) {
        return potService.validatePot(transactionData);
    }
}
