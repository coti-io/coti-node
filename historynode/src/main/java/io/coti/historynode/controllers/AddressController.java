package io.coti.historynode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetAddressesRequest;
import io.coti.historynode.services.HistoryAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<IResponse> getUsedAddresses(@Valid @RequestBody GetAddressesRequest getAddressesRequest) {
        return historyAddressService.getAddressesFromHistory(getAddressesRequest.getAddressesHashes());
    }
}
