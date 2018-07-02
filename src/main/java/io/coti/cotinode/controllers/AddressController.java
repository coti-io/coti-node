package io.coti.cotinode.controllers;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddAddressRequest;
import io.coti.cotinode.http.AddAddressResponse;
import io.coti.cotinode.http.HttpStringConstants;
import io.coti.cotinode.service.interfaces.IAddressService;
import io.coti.cotinode.service.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
    public ResponseEntity<AddAddressResponse> addAddress(@Valid @RequestBody AddAddressRequest addAddressRequest) {
        try {
            if (addressLengthValidation(addAddressRequest.getAddress())) {
                if (addressService.addNewAddress(addAddressRequest.getAddress())) {
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new AddAddressResponse(addAddressRequest.getAddress(),
                                    HttpStringConstants.ADDRESS_CREATED_MESSAGE));
                }
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new AddAddressResponse(addAddressRequest.getAddress(),
                                HttpStringConstants.ADDRESS_ALREADY_EXISTS_MESSAGE));
            } else {
                log.error("Address {} had length error. length: {}", addAddressRequest.getAddress(),
                        addAddressRequest.getAddress().getBytes().length);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new AddAddressResponse(addAddressRequest.getAddress()
                                , HttpStringConstants.ADDRESS_LENGTH_ERROR_MESSAGE, HttpStringConstants.STATUS_ERROR));

            }

        } catch (Exception ex) {
            log.error("Address {} had an error in creation", addAddressRequest.getAddress(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AddAddressResponse(addAddressRequest.getAddress()
                            , HttpStringConstants.ADDRESS_CREATION_ERROR_MESSAGE, HttpStringConstants.STATUS_ERROR));
        }

    }


    private boolean addressLengthValidation(Hash address) {

        return validationService.validateAddressLength(address);
    }


}