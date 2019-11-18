package io.coti.financialserver.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
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
    private ITransactionHelper transactionHelper;
    @Autowired
    private ReceiverBaseTransactionOwners receiverBaseTransactionOwners;
    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private MintingService mintingService;

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
        switch (transactionData.getType()) {
            case Payment:
                ReceiverBaseTransactionOwnerData rbtOwnerData = receiverBaseTransactionOwners.getByHash(transactionHelper.getReceiverBaseTransactionHash(transactionData));
                if (rbtOwnerData == null) {
                    log.error("Owner(merchant) not found for RBT hash in received transaction.", transactionData);
                } else {
                    rollingReserveService.setRollingReserveReleaseDate(transactionData, rbtOwnerData.getMerchantHash());
                }
                break;
            case TokenGeneration:
                currencyService.addToTokenGenerationTransactionQueue(transactionData);
                break;
            case TokenMinting:
                mintingService.addToPropagatedTokenMintingFeeTransactionQueue(transactionData);
                break;
        }
    }

}
