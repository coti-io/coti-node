package io.coti.fullnode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.AddressStatus;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.AddAddressResponse;
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_ADDRESS;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> addAddress(@Valid @RequestBody AddressRequest addAddressRequest) {

        if (addressService.validateAddress(addAddressRequest.getAddress())) {
            if (addressService.addAddress(addAddressRequest.getAddress())) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.Created));
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.Exists));
        } else {
            log.error("Address {} had length error. length: {}", addAddressRequest.getAddress(),
                    addAddressRequest.getAddress().getBytes().length);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(String.format(INVALID_ADDRESS, addAddressRequest.getAddress()), STATUS_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AddressesExistsResponse> addressExists(@Valid @RequestBody AddressBulkRequest addressRequest) {
        List<Hash> addressHashes = addressRequest.getAddresses();
        AddressesExistsResponse addressResponse = new AddressesExistsResponse();

        addressHashes.forEach(addressHash -> {
            boolean result = addressService.addressExists(addressHash);
            addressResponse.addAddressToResult(addressHash.toHexString(), result);
        });

        return ResponseEntity.status(HttpStatus.OK).body(addressResponse);
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST)
    public ResponseEntity<AddressesExistsResponse> addressesExists(@Valid @RequestBody AddressBulkRequest addressRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.addressesExist(addressRequest));
    }
}