package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
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
import java.util.Map;

@Data
@Service
public class AddressStorageValidationService extends EntityStorageValidationService implements IAddressStorageValidationService {

    private ObjectMapper mapper;

    @Autowired
    private BaseNodeValidationService validationService;

    @Autowired
    private AddressTransactionsHistoryService addressTransactionsHistoryService;

//    private final static String ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME = "addressTransactionsHistoryData";
    private final static String ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME = "addressData";

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
        AddressTransactionsHistory addressTxHistory = null;
        try {
            addressTxHistory = mapper.readValue(addressAsJson, AddressTransactionsHistory.class);
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
        return addressTransactionsHistoryService;
    }

    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, HistoryNodeConsensusResult historyNodeConsensusResult)
    {
        return retrieveObjectFromStorage(hash, historyNodeConsensusResult, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME);
    }

    public Map<Hash, ResponseEntity<IResponse>> retrieveMultipleObjectsFromStorage(List<Hash> hashes, HistoryNodeConsensusResult historyNodeConsensusResult) {
        return retrieveMultipleObjectsFromStorage(hashes, historyNodeConsensusResult, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME);
    }

}
