package io.coti.fullnode.controllers;

import com.sun.javaws.exceptions.InvalidArgumentException;
import io.coti.common.http.data.GetAddressData;
import io.coti.common.data.Hash;
import io.coti.common.http.*;
import io.coti.common.http.data.AddressStatus;
import io.coti.common.services.interfaces.IAddressService;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;
import java.util.Vector;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IValidationService validationService;



    @RequestMapping(method = PUT)
    public ResponseEntity<AddAddressResponse> addAddress(@Valid @RequestBody AddressRequest addAddressRequest) throws InvalidArgumentException{

            if (addressLengthValidation(addAddressRequest.getAddress())) {
                if (addressService.addNewAddress(addAddressRequest.getAddress())) {
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

                throw new InvalidArgumentException(new String[]{ String.format(addAddressRequest.getAddress().toHexString(),
                        HttpStringConstants.ADDRESS_INVALID_ERROR_MESSAGE)});
            }
    }


    @RequestMapping(method = POST)
    public ResponseEntity<AddressesExistsResponse> addressExists(@Valid @RequestBody AddressBulkRequest addressRequest) {
            Hash[] addressesHash = addressRequest.getAddresses();
        List<GetAddressData> addressesResults = new Vector<>();

        for (Hash addressHash : addressesHash) {
            boolean result = addressService.addressExists(addressHash);

            addressesResults.add(new GetAddressData(addressHash.toHexString(),result));
        }
            return ResponseEntity.status(HttpStatus.OK).body(new AddressesExistsResponse(addressesResults));
    }


    private boolean addressLengthValidation(Hash address) {

        return validationService.validateAddress(address);
    }


}