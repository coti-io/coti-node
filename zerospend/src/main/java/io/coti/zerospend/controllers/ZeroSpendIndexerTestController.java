package io.coti.zerospend.controllers;

import io.coti.common.communication.DspVote;
import io.coti.common.data.TransactionData;
import io.coti.common.http.AddTransactionRequest;
import io.coti.zerospend.services.DspPropagationService;
import io.coti.zerospend.services.DspVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ZeroSpendIndexerTestController {

    @Autowired
    private DspVoteService dspVoteService;

    @Autowired
    private DspPropagationService dspPropagationService;

    @RequestMapping("/save")
    public String testZSsave(@Valid @RequestBody AddTransactionRequest addTransactionRequest){
        TransactionData transactionData = new TransactionData(addTransactionRequest.baseTransactions,
                addTransactionRequest.hash, addTransactionRequest.transactionDescription,
                addTransactionRequest.senderTrustScore, addTransactionRequest.createTime);

        dspPropagationService.handlePropagatedTransactionFromDSP(transactionData);
        return "Transaction was saved successfully! ";
    }


    @RequestMapping("/vote")
    public String testZSinsertVote(@Valid @RequestBody DspVote dspVote){
        dspVoteService.insertVoteHelper(dspVote);
        return "vote succeed ! ";

    }



}
