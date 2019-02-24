package io.coti.storagenode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.AddEntityRequest;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.interfaces.IResponse;

import io.coti.storagenode.services.AddressStorageValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressStorageValidationService addressStorageValidationService;

    @RequestMapping(value = "/address", method = PUT)
    public ResponseEntity<IResponse> storeAddressToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return addressStorageValidationService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson(), addAddEntityRequest.getHistoryNodeConsensusResult() );
    }

    @RequestMapping(value = "/address", method = GET)
    public ResponseEntity<IResponse> getAddressFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return addressStorageValidationService.retrieveObjectFromStorage(getEntityRequest.getHash(),
                getEntityRequest.getHistoryNodeConsensusResult());
    }


    @RequestMapping(value = "/addresses", method = PUT)
    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return addressStorageValidationService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap(),
                addEntitiesBulkRequest.getHistoryNodeConsensusResult() );
    }


    @RequestMapping(value = "/addresses", method = GET)
    public Map<Hash, ResponseEntity<IResponse>> getAddressesFromStorage(@Valid @RequestBody GetEntitiesBulkRequest getEntitiesBulkRequest) {
        return addressStorageValidationService.retrieveMultipleObjectsFromStorage(getEntitiesBulkRequest.getHashes(),
                getEntitiesBulkRequest.getHistoryNodeConsensusResult());
    }



}
