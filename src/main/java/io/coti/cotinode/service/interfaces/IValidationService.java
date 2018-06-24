package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Sign;

@Service
public interface IValidationService {

    boolean validateSenderAddress(String message, Sign.SignatureData signatureData, Hash addressHash);

    boolean validateSource(TransactionData transactionData);
}