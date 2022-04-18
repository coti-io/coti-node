package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INodeFeesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/fee")
public class BaseNodeFeeController {

    @Autowired
    INodeFeesService nodeFeesService;

    @GetMapping()
    public ResponseEntity<IResponse> getFeesValues() {
        return nodeFeesService.getNodeFees();
    }

}
