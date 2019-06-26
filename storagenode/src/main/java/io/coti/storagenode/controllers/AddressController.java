package io.coti.storagenode.controllers;

import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;

import io.coti.storagenode.services.AddressStorageValidationService;
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
    private AddressStorageValidationService addressStorageValidationService;

    @RequestMapping(value = "/address", method = PUT)
    public ResponseEntity<IResponse> storeAddressToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
        return addressStorageValidationService.storeObjectToStorage(addAddEntityRequest.getHash(),
                addAddEntityRequest.getEntityJson());
    }

    @RequestMapping(value = "/address", method = GET)
    public ResponseEntity<IResponse> getAddressFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return addressStorageValidationService.retrieveObjectFromStorage(getEntityRequest.getHash()
        );
    }


    @RequestMapping(value = "/addresses", method = PUT)
    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
        log.info(" Reached storeMultipleAddressToStorage with addEntitiesBulkRequest = {}", addEntitiesBulkRequest.toString());
        return addressStorageValidationService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap()
        );
    }


    @GetMapping(value = "/addresses")
//    public Map<Hash, ResponseEntity<IResponse>> getAddressesFromStorage(@Valid @RequestBody GetEntitiesBulkRequest getEntitiesBulkRequest) {
        public GetEntitiesBulkResponse getAddressesFromStorage(GetEntitiesBulkRequest getEntitiesBulkRequest) {
        log.info(" Reached getAddressesFromStorage with getEntitiesBulkRequest = {}", getEntitiesBulkRequest.toString());
        return addressStorageValidationService.retrieveMultipleObjectsFromStorage(getEntitiesBulkRequest.getHashes()
        );
    }



}
