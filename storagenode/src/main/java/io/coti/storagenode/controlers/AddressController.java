package io.coti.storagenode.controlers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.AddObjectRequest;
import io.coti.storagenode.http.AddObjectsBulkRequest;
import io.coti.storagenode.http.GetObjectRequest;
import io.coti.storagenode.http.GetObjectsBulkRequest;
import io.coti.storagenode.services.AddressService;
import io.coti.storagenode.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private IAddressService addressService;

    @RequestMapping(value = "/addresses", method = PUT)
    public ResponseEntity<IResponse> addOrUpdateAddresses(@Valid @RequestBody AddObjectsBulkRequest addObjectsBulkRequest) {
        return addressService.insertMultiAddresses(addObjectsBulkRequest.hashToObjectJsonDataMap);
    }

    @RequestMapping(value = "/address", method = PUT)
    public ResponseEntity<IResponse> addOrUpdateAddress(@Valid @RequestBody AddObjectRequest addAddObjectRequest) {
        return addressService.insertAddressJson(addAddObjectRequest.getHash(), addAddObjectRequest.getObjectJson());
    }

    @RequestMapping(value = "/addresses", method = GET)
    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetObjectsBulkRequest getObjectsBulkRequest) {
        return addressService.getMultiAddressesFromDb(getObjectsBulkRequest.hashes);
    }

    @RequestMapping(value = "/address", method = GET)
    public ResponseEntity<IResponse> getAddress(@Valid @RequestBody GetObjectRequest getObjectRequest) {
        return addressService.getAddressByHash(getObjectRequest.hash);
    }

    @RequestMapping(value = "/addresses", method = DELETE)
    public ResponseEntity<IResponse> deleteAddresses(@Valid @RequestBody GetObjectsBulkRequest getObjectsBulkRequest) {
        return addressService.deleteMultiAddressesFromDb(getObjectsBulkRequest.hashes);
    }

    @RequestMapping(value = "/address", method = DELETE)
    public ResponseEntity<IResponse> deleteAddress(@Valid @RequestBody GetObjectRequest getObjectRequest) {
        return addressService.deleteAddressByHash(getObjectRequest.hash);
    }
}
