package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeFeesService;

@Slf4j
@RestController
@RequestMapping("/fee")
public class BaseNodeFeeController {

    @GetMapping()
    public ResponseEntity<IResponse> getFeesValues() {
        return nodeFeesService.getNodeFees();
    }

}
