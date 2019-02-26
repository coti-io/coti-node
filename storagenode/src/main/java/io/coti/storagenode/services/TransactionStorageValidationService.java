package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ValidationService;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TransactionStorageValidationService extends EntityStorageValidationService implements ITransactionStorageValidationService
{
    @Autowired
    private ObjectService objectService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private Transactions transactions;

    public boolean isObjectDIOK(Hash objectHash, String txAsJson)
    {
        //Check for Data Integrity of the Tx
        // TODO implement method also based on objectAsJson
        // TODO specific difference for Tx from Address

        TransactionData txData = null;
        try {
            txData = new ObjectMapper().readValue(txAsJson, TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        boolean valid = validationService.validateTransactionDataIntegrity(txData);

        return valid;
    }

}
