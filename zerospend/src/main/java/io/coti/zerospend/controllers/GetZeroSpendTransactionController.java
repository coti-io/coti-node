package io.coti.zerospend.controllers;

import io.coti.common.data.TransactionData;
import io.coti.common.http.GetZeroSpendTransactionsRequest;
import io.coti.zerospend.services.interfaces.IGetZeroSpendTrxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/get_transaction")
public class GetZeroSpendTransactionController {


    @Autowired
    private IGetZeroSpendTrxService getZeroSpendTrxService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<TransactionData> getZeroSpendTransaction(GetZeroSpendTransactionsRequest
                                                                           getZeroSpendTransactionsRequest) {


        return getZeroSpendTrxService
                .generateZeroSpendTrx(getZeroSpendTransactionsRequest);

    }

}
