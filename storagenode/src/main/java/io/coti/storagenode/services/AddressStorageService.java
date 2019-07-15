package io.coti.storagenode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.crypto.AddressCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Service
@Slf4j
public class AddressStorageService extends EntityStorageService {

    private ObjectMapper mapper;

    @Autowired
    private BaseNodeValidationService validationService;

    @Autowired
    private AddressCrypto addressCrypto;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        super.objectType = ElasticSearchData.ADDRESSES;
    }

    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(GetAddressesBulkRequest getAddressesBulkRequest){
        if(!addressCrypto.verifyGetAddressRequestSignatureMessage(getAddressesBulkRequest)){
            return generateResponse(HttpStatus.UNAUTHORIZED, new GetAddressesBulkResponse(new HashMap<>(), BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }
        return super.retrieveMultipleObjectsFromStorage(getAddressesBulkRequest.getAddressesHash());
    }

    //TODO 7/14/2019 astolia: if failed to map to json. dont continue to elastic. return some failed response
    public ResponseEntity<IResponse> storeMultipleAddressesToStorage(AddAddressesBulkRequest addresses) {
        Map<Hash,String> addressHashToJsonString = new HashMap<>();
        Map<Hash,Boolean> addressFailedConversionToFalse = new HashMap<>();
        addresses.getAddresses().forEach( address -> {
            String jsonStrAddress = getAddressJsonString(address);
            if(jsonStrAddress != ""){
                addressHashToJsonString.put(address.getHash(),jsonStrAddress);
            }
            else{
                addressFailedConversionToFalse.put(address.getHash(),Boolean.FALSE);
            }

        });
        ResponseEntity<IResponse> response = objectService.insertMultiObjects(addressHashToJsonString, false, objectType);
        if(!isResponseOK(response)) {
            return response; // TODO consider some retry mechanism
        }
        response = objectService.insertMultiObjects(addressHashToJsonString, true, objectType);
        if( !isResponseOK(response) ) {
            return response; // TODO consider some retry mechanism, consider removing from ongoing storage
        }
        //TODO 7/15/2019 astolia: add the failed conversion.
        //TODO 7/15/2019 astolia: convert hash,string to hash,boolean
        return response;
    }

    public boolean isObjectDIOK(Hash addressHash, String addressAsJson) {
        AddressData addressTxHistory = null;
        try {
            addressTxHistory = mapper.readValue(addressAsJson, AddressData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // TODO: Commented actual check until Addresses are properly signed
        return validationService.validateAddress(addressTxHistory.getHash()); // TODO add Validation for addressAsJson
    }

    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash) {
        return super.retrieveObjectFromStorage(hash, ElasticSearchData.ADDRESSES);
    }

    public Map<Hash, AddressData> getObjectsMapFromJsonMap(HashMap<Hash, String> responsesMap) {
        Map<Hash, AddressData> hashAddressDataMap = responsesMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> {
            try {
                return mapper.readValue(e.getValue(), AddressData.class);
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }));

        return hashAddressDataMap;
    }

    private String getAddressJsonString(AddressData address){
        try {
            return mapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            log.error("failed to generate json string for address data");
            return "";
        }
    }

    private <T extends BulkResponse> ResponseEntity<IResponse> generateResponse(HttpStatus httpStatus, T response){
        return ResponseEntity.status(httpStatus).body(response);
    }

    @Override
    protected GetAddressesBulkResponse getEmptyEntitiesBulkResponse(){
        return new GetAddressesBulkResponse();
    }

    @Override
    protected GetAddressesBulkResponse getEntitiesBulkResponse(Map<Hash, String> responsesMap){
        //TODO 7/15/2019 astolia: String to AddressData
        return new GetAddressesBulkResponse();
    }
}
