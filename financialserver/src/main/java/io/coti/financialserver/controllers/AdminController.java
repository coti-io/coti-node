package io.coti.financialserver.controllers;


import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.FundDistributionRequest;
import io.coti.financialserver.services.DistributeFundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    DistributeFundService distributeFundService;

    @RequestMapping(path = "/distribution/removeFunds", method = RequestMethod.POST)
    public ResponseEntity<IResponse> distributeFunds(@ModelAttribute @Valid FundDistributionRequest request) {
        return distributeFundService.removeFundDistributionFromFile(request);
    }
}
