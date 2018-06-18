package io.coti.cotinode.controllers;

import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;
import io.coti.cotinode.service.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private IBalanceService balanceService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public GetBalancesResponse getBalances(@RequestBody GetBalancesRequest getBalancesRequest) {
        if (getBalancesRequest == null || getBalancesRequest.addresses == null) {
            return new GetBalancesResponse(
                    HttpStatus.BAD_REQUEST,
                    "Incorrect message arguments"
            );
        }
        log.info(getBalancesRequest.toString());

        return balanceService.getBalances(getBalancesRequest);
    }
}
