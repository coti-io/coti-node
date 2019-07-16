package io.coti.storagenode.controllers;

import io.coti.basenode.http.AddAddressesBulkRequest;
import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.AddressStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressStorageService addressStorageService;

    @PostMapping(value = "/addresses")
    public ResponseEntity<IResponse> getAddressesFromStorage(@Valid @RequestBody GetAddressesBulkRequest getAddressesBulkRequest) {
        return addressStorageService.retrieveMultipleObjectsFromStorage(getAddressesBulkRequest);
    }

    @PutMapping(value = "/addresses")
    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddAddressesBulkRequest addAddressesRequest) {
        return addressStorageService.storeMultipleAddressesToStorage(addAddressesRequest);
    }

}
