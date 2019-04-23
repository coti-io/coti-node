package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionSenderCrypto;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;


@Service
public class ValidationService implements IValidationService {
    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private TransactionSenderCrypto transactionSenderCrypto;
    @Autowired
    private IPotService potService;

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
    public boolean validateTransactionDataIntegrity(TransactionData transactionData) {
        return transactionHelper.validateTransactionType(transactionData) && transactionHelper.validateTransactionCrypto(transactionData)
                && transactionHelper.validateBaseTransactionsDataIntegrity(transactionData) && validateTransactionSenderSignature(transactionData);
    }

    @Override
    public boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData) {
        return validateTransactionDataIntegrity(transactionData) && validateTransactionNodeSignature(transactionData) &&
                //validateTransactionTrustScore(transactionData) &&
                validateBaseTransactionAmounts(transactionData) && validatePot(transactionData);
    }

    @Override
    public boolean validateTransactionNodeSignature(TransactionData transactionData) {
        return transactionCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionSenderSignature(TransactionData transactionData) {
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || transactionSenderCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionTrustScore(TransactionData transactionData) {
        return transactionHelper.validateTrustScore(transactionData);
    }

    @Override
    public boolean validateBaseTransactionAmounts(TransactionData transactionData) {
        return transactionHelper.validateBaseTransactionAmounts(transactionData.getBaseTransactions());
    }

    @Override
    public boolean validateBalancesAndAddToPreBalance(TransactionData transactionData) {
        return transactionHelper.checkBalancesAndAddToPreBalance(transactionData);
    }

    @Override
    public <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData) {
        return transactionHelper.validateBaseTransactionTrustScoreNodeResult(baseTransactionData);
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
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || potService.validatePot(transactionData);
    }
}
