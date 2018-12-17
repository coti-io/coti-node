package io.coti.financialserver.controllers;

import io.coti.financialserver.http.GetRollingReserveMerchantAddressRequest;
import io.coti.financialserver.services.CommentService;
import io.coti.financialserver.services.RollingReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/rollingReserve")
public class RollingReserveController {

    @Autowired
    RollingReserveService rollingReserveService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity getRollingReserveMerchantAddress(@Valid @RequestBody GetRollingReserveMerchantAddressRequest request) {

        return rollingReserveService.geMerchantAddress(request);
    }
}
