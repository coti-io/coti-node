package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.ValidationService;
import io.coti.storagenode.services.interfaces.IAddressStorageValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;


@Service
public class AddressStorageValidationService extends EntityStorageValidationService implements IAddressStorageValidationService {

    private ObjectMapper mapper;

    @Autowired
    private ValidationService validationService;

    @PostConstruct
    public void init()
    {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isObjectDIOK(Hash addressHash, String addressAsJson)
    {
        //Check for Data Integrity of the address
        AddressTransactionsHistory addressTxHistory = null;
        try {
            addressTxHistory = mapper.readValue(addressAsJson, AddressTransactionsHistory.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return validationService.validateAddress(addressTxHistory.getHash()); // TODO add Validation for addressAsJson
    }

}
