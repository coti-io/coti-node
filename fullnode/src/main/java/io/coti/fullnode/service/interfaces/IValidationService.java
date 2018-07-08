package io.coti.fullnode.service.interfaces;

import io.coti.common.data.Hash;
import org.web3j.crypto.Sign;

public interface IValidationService {

    boolean validateSenderAddress(String message, Sign.SignatureData signatureData, Hash addressHash);

    boolean validateSource(Hash hash);

    boolean validateAddressLength(Hash address);
}