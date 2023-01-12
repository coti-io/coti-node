package io.coti.historynode.controllers;

import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.historynode.services.NodeServiceManager.addressService;

@Slf4j
@RestController
public class AddressController {

    @PostMapping(value = "/addresses")
    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetHistoryAddressesRequest getHistoryAddressesRequest) {
        return addressService.getAddresses(getHistoryAddressesRequest);
    }

}
