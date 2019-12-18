package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import io.coti.trustscore.services.RollingReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/rollingReserve")
public class RollingReserveController {

    @Autowired
    private RollingReserveService rollingReserveService;

    @PutMapping
    public ResponseEntity<IResponse> createRollingReserveFee(@Valid @RequestBody RollingReserveRequest request) {
        return rollingReserveService.createRollingReserveFee(request);
    }

    @PostMapping
    public ResponseEntity<IResponse> validateRollingReserveFee(@Valid @RequestBody RollingReserveValidateRequest request) {
        return rollingReserveService.validateRollingReserve(request);
    }
}
