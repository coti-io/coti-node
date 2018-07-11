package io.coti.dspnode.services;

import io.coti.common.data.TransactionData;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.common.http.HttpStringConstants.PARTIAL_VALIDATION_FAILED;
import static io.coti.common.http.HttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class TransactionReceiverService {

    @Autowired
    TransactionPropagationService transactionPropagationService;
    @Autowired
    private IValidationService validationService;

    public ResponseEntity addPropagatedTransactionFromFullNode(TransactionData transactionData) {
        log.info("New transaction request is being processed. Transaction Hash: {}", transactionData.getHash());
        if (validationService.partialValidation(transactionData)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            PARTIAL_VALIDATION_FAILED));
        }
        transactionData.setTrustChainConsensus(false);
        transactionData.setDspConsensus(false);


        return null;
    }

    public void addPropagatedTransactionFromDsps() {

    }

    public void receiveVoteForTransaction() {

    }
}
