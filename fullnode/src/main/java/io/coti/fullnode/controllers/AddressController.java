package io.coti.fullnode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.AddressBulkRequest;
import io.coti.basenode.http.AddressRequest;
import io.coti.basenode.http.AddressesExistsResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.AddressStatus;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.AddAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_ADDRESS;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.fullnode.services.NodeServiceManager.addressService;

@Slf4j
@RestController
@RequestMapping("/address")
public class AddressController {

    @PutMapping()
    public ResponseEntity<IResponse> addAddress(@Valid @RequestBody AddressRequest addAddressRequest) {

        if (addressService.validateAddress(addAddressRequest.getAddress())) {
            if (Boolean.TRUE.equals(addressService.addAddress(addAddressRequest.getAddress()))) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.CREATED));
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.EXISTS));
        } else {
            log.error("Address {} had length error. length: {}", addAddressRequest.getAddress(),
                    addAddressRequest.getAddress().getBytes().length);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(String.format(INVALID_ADDRESS, addAddressRequest.getAddress()), STATUS_ERROR));
        }
    }

    @PostMapping()
    public ResponseEntity<AddressesExistsResponse> addressExists(@Valid @RequestBody AddressBulkRequest addressRequest) {
        List<Hash> addressHashes = addressRequest.getAddresses();
        AddressesExistsResponse addressResponse = new AddressesExistsResponse();

        addressHashes.forEach(addressHash -> {
            boolean result = addressService.addressExists(addressHash);
            addressResponse.addAddressToResult(addressHash.toHexString(), result);
        });

        return ResponseEntity.status(HttpStatus.OK).body(addressResponse);
    }

    @PostMapping(value = "/history")
    public ResponseEntity<AddressesExistsResponse> addressesCheckExistenceAndRequestHistoryNode(@Valid @RequestBody AddressBulkRequest addressRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.addressesCheckExistenceAndRequestHistoryNode(addressRequest));
    }
}