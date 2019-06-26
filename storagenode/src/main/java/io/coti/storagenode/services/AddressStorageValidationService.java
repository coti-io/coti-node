package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.services.interfaces.IAddressStorageValidationService;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Data
@Service
public class AddressStorageValidationService extends EntityStorageValidationService implements IAddressStorageValidationService {

    private ObjectMapper mapper;

    @Autowired
    private BaseNodeValidationService validationService;

    @Autowired
//    private AddressTransactionsHistoryService addressTransactionsHistoryService;
    private AddressService addressService;

//    private final static String ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME = "addressTransactionsHistoryData";
    public final static String ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME = "addressData";
    public final static String ADDRESS_TRANSACTION_HISTORY_INDEX_NAME = "address";

    @PostConstruct
    public void init()
    {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isObjectDIOK(Hash addressHash, String addressAsJson)
    {
        //Check for Data Integrity of the address
        AddressData addressTxHistory = null;
        try {
//            addressTxHistory = mapper.readValue(addressAsJson, AddressTransactionsHistory.class);
            addressTxHistory = mapper.readValue(addressAsJson, AddressData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // TODO: Commented actual check until Addresses are properly signed
//        return validationService.validateAddress(addressTxHistory.getHash()); // TODO add Validation for addressAsJson
        return true;
    }

    @Override
    public ObjectService getObjectService() {
        return addressService;
    }

    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash)
    {
        return retrieveObjectFromStorage(hash, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME);
    }

    public GetEntitiesBulkResponse retrieveMultipleObjectsFromStorage(List<Hash> hashes) {
        return (GetEntitiesBulkResponse) retrieveMultipleObjectsFromStorage(hashes, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME).getBody();
    }

}
