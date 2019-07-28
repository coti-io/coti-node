package io.coti.storagenode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Data
@Service
@Slf4j
public class AddressStorageService extends EntityStorageService {

    //TODO 7/25/2019 astolia: use JacksonSerializer instead
    private ObjectMapper mapper;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private BaseNodeValidationService validationService;

    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;

    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        super.objectType = ElasticSearchData.ADDRESSES;
    }

    public ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        try {
            if (!getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SeriazableResponse(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Map<Hash, AddressData> hashToAddressDataMap = new HashMap<>();
            super.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest.getAddressHashes()).forEach((hash, addressString) -> hashToAddressDataMap.put(hash, jacksonSerializer.deserialize(addressString)));
            GetHistoryAddressesResponse getHistoryAddressesResponse = new GetHistoryAddressesResponse(hashToAddressDataMap);
            getHistoryAddressesResponseCrypto.signMessage(getHistoryAddressesResponse);
            return ResponseEntity.ok(getHistoryAddressesResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SeriazableResponse(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> storeMultipleAddressesToStorage(AddHistoryAddressesRequest addresses) {
        //TODO 7/17/2019 astolia: validate addresses
        Map<Hash, String> addressHashToJsonString = new HashMap<>();
        Map<Hash, Boolean> addressFailedConversionToFalse = new HashMap<>();

        addresses.getAddresses().forEach(address ->
                mapAddressToStringAndFillMaps(address, addressHashToJsonString, addressFailedConversionToFalse));

        if (addressFailedConversionToFalse.size() == addresses.getAddresses().size()) {
            return ResponseEntity.status(HttpStatus.OK).body(new AddHistoryEntitiesResponse());
        }

        ResponseEntity<IResponse> response = objectService.insertMultiObjects(addressHashToJsonString, false, objectType);
        if (!isResponseOK(response)) {
            return super.convertResponseStatusToBooleanAndAddFailedHashes(response, addressFailedConversionToFalse); // TODO consider some retry mechanism
        }
        response = objectService.insertMultiObjects(addressHashToJsonString, true, objectType);
        if (!isResponseOK(response)) {
            return super.convertResponseStatusToBooleanAndAddFailedHashes(response, addressFailedConversionToFalse); // TODO consider some retry mechanism, consider removing from ongoing storage
        }

        return super.convertResponseStatusToBooleanAndAddFailedHashes(response, addressFailedConversionToFalse);
    }

    public boolean validateObjectDataIntegrity(Hash addressHash, String addressAsJson) {
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

//    public ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash) {
//        return super.retrieveObjectFromStorage(hash, ElasticSearchData.ADDRESSES);
//    }

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

    private void mapAddressToStringAndFillMaps(AddressData address, Map<Hash, String> addressHashToJsonString, Map<Hash, Boolean> addressFailedConversionToFalse) {
        try {
            String addressJsonString = mapper.writeValueAsString(address);
            addressHashToJsonString.put(address.getHash(), addressJsonString);
        } catch (JsonProcessingException e) {
            log.error("failed to generate json string for address data {}", address.getHash().toString());
            addressFailedConversionToFalse.put(address.getHash(), Boolean.FALSE);
        }
    }

    @Override
    protected GetHistoryAddressesResponse getEmptyEntitiesBulkResponse() {
        return new GetHistoryAddressesResponse();
    }

    @Override
    protected GetHistoryAddressesResponse getEntitiesBulkResponse(Map<Hash, String> responsesMap) {
        Map<Hash, AddressData> respMap = new LinkedHashMap<>();
        responsesMap.entrySet().forEach(entry -> {
            respMap.put(entry.getKey(), entry.getValue() == null ? null : desrializeAddressData(entry.getValue()));
        });
        return new GetHistoryAddressesResponse(respMap);
    }

    private AddressData desrializeAddressData(String addressDataJsonString) {
        AddressData addressData;
        try {
            addressData = mapper.readValue(addressDataJsonString, AddressData.class);
        } catch (IOException e) {
            log.error("Failed to deserialize AddressData");
            addressData = null;
        }
        return addressData;
    }
}
