package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

@Service
public interface IValidationService {

    boolean validateUserHash(Hash hash);

    boolean validateSource(TransactionData transactionData);
}
