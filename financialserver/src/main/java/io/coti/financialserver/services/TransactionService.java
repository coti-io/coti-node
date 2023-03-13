package io.coti.financialserver.services;

import io.coti.basenode.data.ReceiverBaseTransactionOwnerData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.http.DeleteRejectedTransactionsRequest;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.TransactionRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.financialserver.http.TransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.financialserver.http.HttpStringConstants.*;
import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class TransactionService extends BaseNodeTransactionService {

    @Override
    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(TransactionRequest transactionRequest) {

        ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData = transactionRequest.getReceiverBaseTransactionOwnerData();

        if (!receiverBaseTransactionOwnerCrypto.verifySignature(receiverBaseTransactionOwnerData)) {
            log.error("ReceiverBaseTransactionOwner invalid signature for merchant {}", receiverBaseTransactionOwnerData.getMerchantHash());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        receiverBaseTransactionOwners.put(receiverBaseTransactionOwnerData);

        return ResponseEntity.status(HttpStatus.OK).body(new TransactionResponse(STATUS_SUCCESS));
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        continueHandleTransaction(transactionData);
    }

    @Override
    protected void continueHandleMissingTransaction(TransactionData transactionData) {
        continueHandleTransaction(transactionData);
    }

    private void continueHandleTransaction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.Payment) {
            ReceiverBaseTransactionOwnerData rbtOwnerData = receiverBaseTransactionOwners.getByHash(nodeTransactionHelper.getReceiverBaseTransactionHash(transactionData));
            if (rbtOwnerData == null) {
                log.error("Owner(merchant) not found for RBT hash in received transaction {}.", transactionData.getHash());
            } else {
                rollingReserveService.setRollingReserveReleaseDate(transactionData, rbtOwnerData.getMerchantHash());
            }
        }
    }

    @Override
    public ResponseEntity<IResponse> getRejectedTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> deleteRejectedTransactions(DeleteRejectedTransactionsRequest deleteRejectedTransactionsRequest) {
        throw new UnsupportedOperationException();
    }
}
