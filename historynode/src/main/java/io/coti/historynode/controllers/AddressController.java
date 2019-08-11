package io.coti.historynode.controllers;

import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @RequestMapping(value = "/addresses", method = POST)
    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetHistoryAddressesRequest getHistoryAddressesRequest) {
        return addressService.getAddresses(getHistoryAddressesRequest);
    }

}
