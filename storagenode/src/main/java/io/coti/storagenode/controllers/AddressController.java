package io.coti.storagenode.controllers;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesRequest;
import io.coti.basenode.http.GetAddressesResponse;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.AddressStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressStorageService addressStorageService;

    @RequestMapping(value = "/address", method = PUT)
    public ResponseEntity<IResponse> getAddressFromStorage(@Valid @RequestBody GetEntityRequest getEntityRequest) {
        return addressStorageService.retrieveObjectFromStorage(getEntityRequest.getHash());
    }

    @PostMapping(value = "/addresses")
    public ResponseEntity<GetAddressesResponse> getAddressesFromStorage(@Valid @RequestBody GetAddressesRequest getAddressesRequest) {
        log.info(" Reached getAddressesFromStorage with getEntitiesBulkRequest = {}", getAddressesRequest.toString());
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(new AddTransactionResponse(
//                        STATUS_ERROR,
//                        TRANSACTION_ALREADY_EXIST_MESSAGE));
        Map<Hash, AddressData> map = new HashMap();

        map.put(new Hash(0),new AddressData(new Hash(0)));
        map.put(new Hash(1),new AddressData(new Hash(1)));
        ResponseEntity<GetAddressesResponse> response =  ResponseEntity.status(HttpStatus.OK).body(new GetAddressesResponse(map,"a","b")); //GetAddressesResponse();
        return response;
//        return addressStorageService.retrieveMultipleObjectsFromStorage(new ArrayList(getAddressesRequest.getAddressesHash()));
    }

//    @RequestMapping(value = "/address", method = PUT)
//    public ResponseEntity<IResponse> storeAddressToStorage(@Valid @RequestBody AddEntityRequest addAddEntityRequest) {
//        return addressStorageService.storeObjectToStorage(addAddEntityRequest.getHash(),
//                addAddEntityRequest.getEntityJson());
//    }
//
//    @RequestMapping(value = "/addresses", method = PUT)
//    public ResponseEntity<IResponse> storeMultipleAddressToStorage(@Valid @RequestBody AddEntitiesBulkRequest addEntitiesBulkRequest) {
//        log.info(" Reached storeMultipleAddressToStorage with addEntitiesBulkRequest = {}", addEntitiesBulkRequest.toString());
//        return addressStorageService.storeMultipleObjectsToStorage(addEntitiesBulkRequest.getHashToEntityJsonDataMap());
//    }






}
