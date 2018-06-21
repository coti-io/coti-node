package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

@Service
public interface IValidationService {

    boolean validateUserHash(TransactionData transactionData);

    boolean validateSources(TransactionData transactionData);
}
