package io.coti.financialserver.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.financialserver.crypto.ReceiverBaseTransactionOwnerCrypto;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.http.TransactionResponse;
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
    private RollingReserveService rollingReserveService;
    @Autowired
    private ReceiverBaseTransactionOwners receiverBaseTransactionOwners;

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

        if (transactionData.getType() == TransactionType.Payment) {

            ReceiverBaseTransactionOwnerData rbtOwnerData = receiverBaseTransactionOwners.getByHash(transactionData.getReceiverBaseTransactionHash());
            if (rbtOwnerData == null) {
                log.error("Owner(merchant) not found for RBT hash in received transaction.", transactionData);
            } else {
                rollingReserveService.setRollingReserveReleaseDate(transactionData, rbtOwnerData.getMerchantHash());
            }
        }
    }
}
