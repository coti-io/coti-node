package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.service.interfaces.IValidationService;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

@Component
public class ValidationService implements IValidationService {

    @Override
    public boolean validateSenderAddress(String message, Sign.SignatureData signatureData, Hash addressHash) {

        try {
            BigInteger publicKey = getAddressFromMessageAndSignature(message, signatureData);
            return Arrays.equals(addressHash.getBytes(), publicKey.toByteArray());

        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }

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
