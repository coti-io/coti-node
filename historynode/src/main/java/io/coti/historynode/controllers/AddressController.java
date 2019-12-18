package io.coti.historynode.controllers;

import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(value = "/addresses")
    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetHistoryAddressesRequest getHistoryAddressesRequest) {
        return addressService.getAddresses(getHistoryAddressesRequest);
    }

}
