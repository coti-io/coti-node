package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.coti.trustscore.services.NodeServiceManager.rollingReserveService;

@RestController
@RequestMapping("/rollingReserve")
public class RollingReserveController {

    @PutMapping()
    public ResponseEntity<IResponse> createRollingReserveFee(@Valid @RequestBody RollingReserveRequest request) {
        return rollingReserveService.createRollingReserveFee(request);
    }

    @PostMapping()
    public ResponseEntity<IResponse> validateRollingReserveFee(@Valid @RequestBody RollingReserveValidateRequest request) {
        return rollingReserveService.validateRollingReserve(request);
    }
}
