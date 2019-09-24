package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.data.UserTokenGenerationData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.financialserver.crypto.ReceiverBaseTransactionOwnerCrypto;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import io.coti.financialserver.http.TransactionRequest;
import io.coti.financialserver.http.TransactionResponse;
import io.coti.financialserver.model.ReceiverBaseTransactionOwners;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private CurrencyService currencyService;
    private BlockingQueue<TransactionData> tokenGenerationTransactionsQueue;
    private Thread tokenGenerationTransactionsThread;

    @Override
    public void init() {
        initTokenGenerationQueueAndThread();
        super.init();
    }

    public void initTokenGenerationQueueAndThread() {
        tokenGenerationTransactionsQueue = new LinkedBlockingQueue<>();
        tokenGenerationTransactionsThread = new Thread(() -> handlePropagatedTokenGenerationTransactions());
        tokenGenerationTransactionsThread.start();
    }

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

            ReceiverBaseTransactionOwnerData rbtOwnerData = receiverBaseTransactionOwners.getByHash(transactionHelper.getReceiverBaseTransactionHash(transactionData));

            if (rbtOwnerData == null) {
                log.error("Owner(merchant) not found for RBT hash in received transaction {}.", transactionData.getHash());
            } else {
                rollingReserveService.setRollingReserveReleaseDate(transactionData, rbtOwnerData.getMerchantHash());
            }
        } else if (transactionData.getType() == TransactionType.TokenGeneration) {
            try {
                tokenGenerationTransactionsQueue.put(transactionData);
            } catch (InterruptedException e) {
                log.error(String.format("Interrupted while waiting for insertion of token generation transaction %s into blocking queue.", transactionData.getHash()));
            }
        }
    }

    private void handlePropagatedTokenGenerationTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData tokenGenerationTransaction = tokenGenerationTransactionsQueue.take();
                Hash userHash = tokenGenerationTransaction.getSenderHash();
                currencyService.addUserToHashLocks(userHash);
                synchronized (currencyService.getUserHashLock(userHash)) {
                    UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
                    if (userTokenGenerationData == null) {
                        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
                        transactionHashToCurrencyMap.put(tokenGenerationTransaction.getHash(), null);
                        userTokenGenerations.put(new UserTokenGenerationData(userHash, transactionHashToCurrencyMap));
                    } else {
                        userTokenGenerationData.getTransactionHashToCurrencyMap().put(tokenGenerationTransaction.getHash(), null);
                        userTokenGenerations.put(userTokenGenerationData);
                    }
                }
                currencyService.removeUserFromHashLocks(userHash);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
