package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import io.coti.trustscore.services.RollingReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/rollingReserve")
public class RollingReserveController {
    @Autowired
    RollingReserveService rollingReserveService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createRollingReserveFee(@Valid @RequestBody RollingReserveRequest request) {
        return rollingReserveService.createRollingReserveFee(request);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> validateRollingReserveFee(@Valid @RequestBody RollingReserveValidateRequest request) {
        return rollingReserveService.validateRollingReserve(request);
    }
}
