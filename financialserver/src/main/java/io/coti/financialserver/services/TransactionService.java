package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.financialserver.crypto.ReceiverBaseTransactionOwnerCrypto;
import io.coti.financialserver.data.FinancialServerTransactionData;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.http.TransactionResponse;
import io.coti.financialserver.model.ConsumerDisputes;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.model.FinancialServerTransactions;
import io.coti.financialserver.model.ReceiverBaseTransactionOwners;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    @Autowired
    private ReceiverBaseTransactionOwnerCrypto receiverBaseTransactionOwnerCrypto;
    @Autowired
    Disputes disputes;
    @Autowired
    ConsumerDisputes consumerDisputes;
    @Autowired
    ReceiverBaseTransactionOwners receiverBaseTransactionOwners;
    @Autowired
    FinancialServerTransactions financialServerTransactions;

    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(TransactionRequest transactionRequest) {

        ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData = transactionRequest.getReceiverBaseTransactionOwnerData();

        receiverBaseTransactionOwnerCrypto.signMessage(receiverBaseTransactionOwnerData);
        if (!receiverBaseTransactionOwnerCrypto.verifySignature(receiverBaseTransactionOwnerData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        receiverBaseTransactionOwners.put(receiverBaseTransactionOwnerData);

        return ResponseEntity.status(HttpStatus.OK).body(new TransactionResponse(STATUS_SUCCESS));
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {

     /*   Hash receiverBaseTransactionAddress = transactionData.getReceiverBaseTransactionAddress();

        // TODO: Check if transaction is of type payment
        if (receiverBaseTransactionAddress != null) {
            FinancialServerTransactionData financialServerTransactionAddress = new FinancialServerTransactionData(receiverBaseTransactionAddress);
            financialServerTransactions.put(financialServerTransactionAddress);
        } */
    }
}
