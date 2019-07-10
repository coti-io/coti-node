package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionsBulkResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.interfaces.ITransactionStorageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
//import jdk.nashorn.internal.parser.JSONParser;
//import net.minidev.json.JSONObject;

@Service
public class TransactionStorageService extends EntityStorageService implements ITransactionStorageValidationService
{

    @Autowired
    private BaseNodeValidationService validationService;

    private ObjectMapper mapper;

    @PostConstruct
    public void init()
    {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        super.objectType = ElasticSearchData.TRANSACTIONS;
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

        boolean valid = true; //TODO: Disabled for testing purposes
//        boolean valid = validationService.validateTransactionDataIntegrity(txDataDeserializedFromES);

        return valid;
    }

    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash) {
        return retrieveObjectFromStorage(hash, ElasticSearchData.TRANSACTIONS);
    }

    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes) {
        return super.retrieveMultipleObjectsFromStorage(hashes, new GetTransactionsBulkResponse());
    }

}
