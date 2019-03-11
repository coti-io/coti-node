package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.ValidationService;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class TransactionStorageValidationService extends EntityStorageValidationService implements ITransactionStorageValidationService
{

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TransactionService transactionService;

    private ObjectMapper mapper;

    @PostConstruct
    public void init()
    {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isObjectDIOK(Hash objectHash, String txAsJson)
    {
        //Check for Data Integrity of the Tx
        // TODO specific difference for Tx from Address

        TransactionData txDataDeserializedFromES = null;
        try {
            txDataDeserializedFromES = mapper.readValue(txAsJson, TransactionData.class);
            int temp = 7;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        boolean valid = validationService.validateTransactionDataIntegrity(txDataDeserializedFromES);

        return valid;
    }

    @Override
    public ObjectService getObjectService() {
        return transactionService;
    }

}
