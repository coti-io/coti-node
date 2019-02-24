package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ValidationService;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionStorageValidationService extends EntityStorageValidationService implements ITransactionStorageValidationService
{
    @Autowired
    private ObjectService objectService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private Transactions transactions;

    public boolean isObjectDIOK(Hash objectHash, String objectAsJson)
    {
        //Check for Data Integrity of the Tx
        // TODO implement method also based on objectAsJson
        // TODO specific difference for Tx from Address

        TransactionData txDataByHash = transactions.getByHash(objectHash);  // TODO gets data from DB?
        boolean valid = validationService.validateTransactionDataIntegrity(txDataByHash);

        return valid;
    }

}
