package io.coti.historynode.controllers;

import io.coti.basenode.http.GetAddressesRequest;
import io.coti.basenode.http.GetAddressesResponse;
import io.coti.historynode.services.HistoryAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private HistoryAddressService historyAddressService;

    @RequestMapping(value = "/addresses", method = PUT)
    public ResponseEntity<GetAddressesResponse> getAddresses(@Valid @RequestBody GetAddressesRequest getAddressesRequest) {
        return historyAddressService.getAddresses(getAddressesRequest);
    }

}
