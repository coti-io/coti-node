package io.coti.cotinode.controllers;

import io.coti.cotinode.http.AddAddressRequest;
import io.coti.cotinode.http.AddAddressResponse;
import io.coti.cotinode.service.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Controller
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private IAddressService addressService;

    @RequestMapping(method = PUT)
    public AddAddressResponse addAddress(@RequestBody AddAddressRequest addAddressRequest) {
        if (addAddressRequest == null || addAddressRequest.addressHash == null) {
            return new AddAddressResponse(
                    HttpStatus.BAD_REQUEST,
                    "Incorrect message arguments"
            );
        }

        if (addressService.addNewAddress(addAddressRequest.addressHash)) {
            return new AddAddressResponse(
                    HttpStatus.OK, String.format("Address %s added successfuly.", addAddressRequest.addressHash));
        }

        return null;
    }
}