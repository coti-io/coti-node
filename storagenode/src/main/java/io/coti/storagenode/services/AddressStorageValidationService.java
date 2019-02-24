package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.storagenode.services.interfaces.IAddressStorageValidationService;

import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class AddressStorageValidationService extends EntityStorageValidationService implements IAddressStorageValidationService {


    public boolean isObjectDIOK(Hash addressHash, String addressAsJson)
    {
        //Check for Data Integrity of the address
        try {
            AddressTransactionsHistory addressTxHistory = new ObjectMapper().readValue(addressAsJson, AddressTransactionsHistory.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
//        CryptoHelper.VerifyByPublicKey() // TODO get Object from Json
        return CryptoHelper.IsAddressValid(addressHash); // TODO add Validation for addressAsJson
    }

}
