package io.coti.storagenode.controllers;

import io.coti.basenode.http.AddHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.storagenode.services.NodeServiceManager.addressStorageService;

@Slf4j
@RestController
public class AddressController {

    @PostMapping(value = "/addresses")
    public ResponseEntity<IResponse> getAddressesFromStorage(@Valid @RequestBody GetHistoryAddressesRequest getHistoryAddressesRequest) {
        return addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
    }

    @PutMapping(value = "/addresses")
    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddHistoryAddressesRequest addAddressesRequest) {
        return addressStorageService.storeMultipleObjectsToStorage(addAddressesRequest.getHashToAddressDataJsonMap());
    }

}
