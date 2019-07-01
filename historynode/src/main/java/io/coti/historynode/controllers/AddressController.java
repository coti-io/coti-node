package io.coti.historynode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetAddressRequest;
import io.coti.historynode.services.HistoryAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private HistoryAddressService historyAddressService;

    @RequestMapping(value = "/addresses", method = GET)
    public ResponseEntity<IResponse> checkAddressValidity(@Valid @RequestBody GetAddressRequest getAddressRequest) {
        return historyAddressService.getAddress(getAddressRequest.getAddressesHash());
    }

    //TODO 7/1/2019 astolia: this endpoint should be used by TransactionService too. Need to move it to common controller.
    @RequestMapping(value = "/addresses", method = GET)
    public ResponseEntity<IResponse> triggerAddressStorage(@Valid @RequestBody GetAddressRequest getAddressRequest) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
