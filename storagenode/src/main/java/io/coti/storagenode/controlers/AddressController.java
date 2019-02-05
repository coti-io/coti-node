package io.coti.storagenode.controlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AddressController {
//
//    @Autowired
//    private AddressService addressService;
//
//    @RequestMapping(value = "/addresses", method = PUT)
//    public ResponseEntity<IResponse> addOrUpdateAddresses(@Valid @RequestBody AddObjectsBulkRequest addObjectsBulkRequest) {
//        return addressService.insertMultiAddresses(addObjectsBulkRequest.hashToObjectJsonDataMap);
//    }
//
//    @RequestMapping(value = "/address", method = PUT)
//    public ResponseEntity<IResponse> addOrUpdateAddress(@Valid @RequestBody AddObjectRequest addAddObjectRequest) {
//        return addressService.insertAddressJson(addAddObjectRequest.getHash(), addAddObjectRequest.getObjectJson());
//    }
//
//    @RequestMapping(value = "/addresses", method = GET)
//    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetObjectsBulkRequest getObjectsBulkRequest) {
//        return addressService.getMultiAddressesFromDb(getObjectsBulkRequest.hashes);
//    }
//
//    @RequestMapping(value = "/address", method = GET)
//    public ResponseEntity<IResponse> getAddress(@Valid @RequestBody GetObjectRequest getObjectRequest) {
//        return addressService.getAddressByHash(getObjectRequest.hash);
//    }
//
//    @RequestMapping(value = "/addresses", method = DELETE)
//    public ResponseEntity<IResponse> deleteAddresses(@Valid @RequestBody GetObjectsBulkRequest getObjectsBulkRequest) {
//        return addressService.deleteMultiAddressesFromDb(getObjectsBulkRequest.hashes);
//    }
//
//    @RequestMapping(value = "/address", method = DELETE)
//    public ResponseEntity<IResponse> deleteAddress(@Valid @RequestBody GetObjectRequest getObjectRequest) {
//        return addressService.deleteAddressByHash(getObjectRequest.hash);
//    }
}
