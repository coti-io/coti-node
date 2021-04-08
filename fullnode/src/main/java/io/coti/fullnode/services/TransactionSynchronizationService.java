package io.coti.fullnode.services;

import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionSynchronizationService;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.fullnode.data.AddressTransactionsByAttachment;
import io.coti.fullnode.model.AddressTransactionsByAttachments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
@Slf4j
public class TransactionSynchronizationService extends BaseNodeTransactionSynchronizationService {

    @Autowired
    private AddressTransactionsByAttachments addressTransactionsByAttachments;
    @Autowired
    private ITransactionService transactionService;

    @Override
    protected void insertMissingTransactions(List<TransactionData> missingTransactions, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, AtomicLong completedMissingTransactionNumber, AtomicBoolean finishedToReceive, int offset) {
        Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap = new ConcurrentHashMap<>();
        Map<Hash, AddressTransactionsByAttachment> addressToTransactionsByAttachmentMap = new ConcurrentHashMap<>();
        Consumer<TransactionData> handleTransactionConsumer = transactionData -> {
            transactionService.handleMissingTransaction(transactionData, trustChainUnconfirmedExistingTransactionHashes, missingTransactionExecutorMap);
            transactionHelper.updateAddressTransactionHistory(addressToTransactionsHistoryMap, transactionData);
            ((TransactionHelper) transactionHelper).updateAddressTransactionByAttachment(addressToTransactionsByAttachmentMap, transactionData);
        };
        handleMissingTransactions(missingTransactions, handleTransactionConsumer, completedMissingTransactionNumber, finishedToReceive, offset);

        insertAddressTransactionsHistory(addressToTransactionsHistoryMap);
        insertAddressTransactionsByAttachment(addressToTransactionsByAttachmentMap);
    }

    private void insertAddressTransactionsByAttachment(Map<Hash, AddressTransactionsByAttachment> addressToTransactionsByAttachmentMap) {
        log.info("Started to insert address transactions by attachment");
        addressTransactionsByAttachments.putBatch(addressToTransactionsByAttachmentMap);
        log.info("Finished to insert address transactions by attachment");
    }
}
