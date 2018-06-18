package io.coti.cotinode.controllers;

import io.coti.cotinode.http.AddAddressRequest;
import io.coti.cotinode.http.AddAddressResponse;
import io.coti.cotinode.http.Response;
import io.coti.cotinode.service.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static io.coti.cotinode.http.Response.ERROR_MESSAGE_INCORRECT_ARGUMENTS;
import static io.coti.cotinode.http.Response.STATUS_ERROR;
import static io.coti.cotinode.http.Response.STATUS_SUCCESS;
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
                    STATUS_ERROR,
                    ERROR_MESSAGE_INCORRECT_ARGUMENTS
            );
        }

        if (addressService.addNewAddress(addAddressRequest.addressHash)) {
            return new AddAddressResponse(
                    STATUS_SUCCESS, String.format("Address %s added successfuly.", addAddressRequest.addressHash));
        }

        return null;
    }
}