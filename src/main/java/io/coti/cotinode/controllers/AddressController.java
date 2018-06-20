package io.coti.cotinode.controllers;

import io.coti.cotinode.http.AddAddressRequest;
import io.coti.cotinode.http.AddAddressResponse;
import io.coti.cotinode.service.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private IAddressService addressService;

    @RequestMapping(method = PUT)
    public AddAddressResponse addAddress(@Valid @RequestBody AddAddressRequest addAddressRequest) {

        if (addressService.addNewAddress(addAddressRequest.addressHash)) {
            return new AddAddressResponse(addAddressRequest.addressHash);
        }

        return null;
    }
}