package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.AddressCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.historynode.http.StoreEntitiesToStorageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;


@Slf4j
@Service
public class HistoryAddressService /*extends EntityService*/ {
    @Autowired
    protected StorageConnector storageConnector;
    @Autowired
    private Addresses addresses;
    @Autowired
    private AddressCrypto addressCrypto;
    @Autowired
    private BaseNodeValidationService validationService;

    protected String endpoint = null;

    @Value("${storage.server.address}")
    protected String storageServerAddress;
    protected ObjectMapper mapper;

    @PostConstruct
    private void init() {
        mapper = new ObjectMapper();
    }

    public ResponseEntity<GetAddressesBulkResponse> getAddresses(GetAddressesBulkRequest getAddressesBulkRequest) {
        if(!addressCrypto.verifyGetAddressRequestSignatureMessage(getAddressesBulkRequest)) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new GetAddressesBulkResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        List<Hash> addressesHashes = getAddressesBulkRequest.getAddressesHash();
        Map<Hash,AddressData> addressToAddressDataResponse = populateAndRemoveFoundAddresses(addressesHashes);
        ResponseEntity<GetAddressesBulkResponse> storageResponse = getAddressesFromStorage(addressesHashes);
        GetAddressesBulkResponse getAddressesBulkResponse =  storageResponse.getBody();

        if(!validationService.validateGetAddressesResponse(getAddressesBulkResponse)){
            return generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, new GetAddressesBulkResponse(new LinkedHashMap<>(),"Response validation failed", BaseNodeHttpStringConstants.STATUS_ERROR));
        }
        //TODO 7/15/2019 astolia: check if fields are null to avoid nullpointer
        if(!addressCrypto.verifyGetAddressResponseSignatureMessage(getAddressesBulkResponse)) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new GetAddressesBulkResponse(new LinkedHashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        addressToAddressDataResponse.putAll((storageResponse.getBody()).getAddressHashesToAddresses());
        return generateResponse(HttpStatus.OK, new GetAddressesBulkResponse(addressToAddressDataResponse, "Addresses successfully retrieved", BaseNodeHttpStringConstants.STATUS_SUCCESS));
    }

    private Map<Hash,AddressData> populateAndRemoveFoundAddresses(List<Hash> addressesHashes){
        Map<Hash,AddressData> addressesFoundInRocksDb = new HashMap<>();

        addressesHashes.forEach(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if(addressData != null){
                addressesFoundInRocksDb.put(addressHash,addressData);
            }
        });
        addressesFoundInRocksDb.keySet().forEach(addressesHashes::remove);
        return addressesFoundInRocksDb;
    }

    private ResponseEntity<GetAddressesBulkResponse> getAddressesFromStorage(List<Hash> addressesHashes) {
        GetAddressesBulkRequest getAddressesBulkRequest = new GetAddressesBulkRequest(addressesHashes);
        addressCrypto.signAddressRequest(getAddressesBulkRequest);
        return  storageConnector.storeInStorage(storageServerAddress + "/addresses",getAddressesBulkRequest, GetAddressesBulkResponse.class);
    }

    private <T extends BulkResponse> ResponseEntity<T> generateResponse(HttpStatus httpStatus, T response){
        return ResponseEntity.status(httpStatus).body(response);
    }

    protected ResponseEntity<StoreEntitiesToStorageResponse> storeEntitiesByType(String url, AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return storageConnector.storeInStorage(storageServerAddress + endpoint, addEntitiesBulkRequest, StoreEntitiesToStorageResponse.class);
    }

}
