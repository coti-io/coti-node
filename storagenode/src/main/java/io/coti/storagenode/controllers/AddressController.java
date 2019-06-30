package io.coti.storagenode.controllers;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;

import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.services.AddressStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressStorageService addressStorageService;

    @RequestMapping(value = "/address", method = GET)
    public ResponseEntity<IResponse> getAddressFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return addressStorageService.retrieveObjectFromStorage(getEntityRequest.getHash());
    }

    @GetMapping(value = "/addresses")
//    public Map<Hash, ResponseEntity<IResponse>> getAddressesFromStorage(@Valid @RequestBody GetEntitiesBulkRequest getEntitiesBulkRequest) {
    public ResponseEntity<IResponse> getAddressesFromStorage(GetEntitiesBulkRequest getEntitiesBulkRequest) {
        log.info(" Reached getAddressesFromStorage with getEntitiesBulkRequest = {}", getEntitiesBulkRequest.toString());
        return addressStorageService.retrieveMultipleObjectsFromStorage(getEntitiesBulkRequest.getHashes());
    }

    @RequestMapping(value = "/address", method = PUT)
    public ResponseEntity<IResponse> storeAddressToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return addressStorageService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson());
    }

    @RequestMapping(value = "/addresses", method = PUT)
    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        log.info(" Reached storeMultipleAddressToStorage with addEntitiesBulkRequest = {}", addEntitiesBulkRequest.toString());
        return addressStorageService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap());
    }






}
