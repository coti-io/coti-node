package io.coti.fullnode.service.interfaces;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;

public interface IValidationService {

    boolean validateBaseTransaction(BaseTransactionData baseTransactionData, Hash transactionHash);

    boolean validateSource(Hash hash);

    boolean validateAddressLength(Hash address);
}