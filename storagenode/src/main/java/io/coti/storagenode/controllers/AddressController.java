package io.coti.storagenode.controllers;

import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.GetAddressesBulkResponse;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.AddressStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;

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
    public ResponseEntity<GetAddressesBulkResponse> getAddressesFromStorage(@Valid @RequestBody GetAddressesBulkRequest getAddressesBulkRequest) {
//        for testing purposes
//        Map<Hash, AddressData> map = new HashMap();
//        map.put(new Hash(0),new AddressData(new Hash(0)));
//        map.put(new Hash(1),new AddressData(new Hash(1)));
//        ResponseEntity<GetAddressesBulkResponse> response =  ResponseEntity.status(HttpStatus.OK).body(new GetAddressesBulkResponse(map,"a","b")); //GetAddressesBulkResponse();
        return addressStorageService.retrieveMultipleObjectsFromStorage(new ArrayList(getAddressesBulkRequest.getAddressesHash()));
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
