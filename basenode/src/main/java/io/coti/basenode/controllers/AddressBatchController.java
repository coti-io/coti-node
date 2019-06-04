package io.coti.basenode.controllers;

import io.coti.basenode.http.AddressFileRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/address/batch")
public class AddressBatchController {
    @Autowired
    private IAddressService addressService;

    @GetMapping()
    public void getAddressBatch(HttpServletResponse response) {
        addressService.getAddressBatch(response);
    }

    @PostMapping()
    public ResponseEntity<IResponse> addAddressBatch(@ModelAttribute @Valid AddressFileRequest request) {
        return addressService.addAddressBatch(request);
    }

}
