package io.coti.fullnode.service;

import io.coti.common.crypto.BasicTransactionCryptoDecorator;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.fullnode.service.interfaces.IValidationService;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SignatureException;

@Component
public class ValidationService implements IValidationService {


    @Override
    public boolean validateBaseTransaction(BaseTransactionData baseTransactionData, Hash transactionHash) {
        BasicTransactionCryptoDecorator baseTransactionCrypto = new BasicTransactionCryptoDecorator(baseTransactionData, transactionHash);
        return baseTransactionCrypto.IsBasicTransactionValid();
    }

    @Override
    public boolean validateSource(Hash hash) {
        return true;
    }

    private BigInteger getAddressFromMessageAndSignature(String signedMessage, Sign.SignatureData signatureData) throws SignatureException {
        return Sign.signedMessageToKey(signedMessage.getBytes(), signatureData);
    }

    @Override
    public boolean validateAddressLength(Hash address) {
        return (address.getBytes().length == 34) && (address.getBytes().length != 0);
    }
}
