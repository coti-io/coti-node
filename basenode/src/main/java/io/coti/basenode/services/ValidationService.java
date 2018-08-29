package io.coti.basenode.services;

import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SignatureException;

@Component
public class ValidationService implements IValidationService {
    @Autowired
    private Transactions transactions;

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

    private BigInteger getAddressFromMessageAndSignature(String signedMessage, Sign.SignatureData signatureData) throws SignatureException {
        return Sign.signedMessageToKey(signedMessage.getBytes(), signatureData);
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
    public boolean validatePow(TransactionData transactionData) {
        return true;
    }
}
