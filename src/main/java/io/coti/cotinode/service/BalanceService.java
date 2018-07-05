package io.coti.cotinode.service;

import io.coti.cotinode.LiveView.LiveViewService;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;
import io.coti.cotinode.http.websocket.WebSocketSender;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.interfaces.IBalanceService;
import io.coti.cotinode.service.interfaces.IClusterService;
import io.coti.cotinode.service.interfaces.IQueueService;
import io.coti.cotinode.service.interfaces.IZeroSpendService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    @Autowired
    private Transactions transactions;

    @Autowired
    private IZeroSpendService zeroSpendService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IQueueService queueService;

    @Autowired
    private ConfirmedTransactions confirmedTransactions;

    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    private WebSocketSender webSocketSender;

    @Autowired
    private LiveViewService liveViewService;

    private Map<Hash, BigDecimal> balanceMap;
    private Map<Hash, BigDecimal> preBalanceMap;

    @PostConstruct
    private void init() {
        try {
            balanceMap = new ConcurrentHashMap<>();
            preBalanceMap = new ConcurrentHashMap<>();

            loadBalanceFromSnapshot();
            deleteConfirmedTransactions();
            // TODO: call RestTemplatePropagation.propagateMultiTransactionFromNeighbor (with no parameter of fromAttachmentTimeStampIfThere AreNoTransactions)
            // Then set all transactions on TCC=0, sort them topological, and  call for  every transaction to TransactionService.addTransactionFromPropagation
            List<Hash> hashesForClusterService;
            if (unconfirmedTransactions.isEmpty()) {
                hashesForClusterService = generateGenesisTransactions();
            } else {
                hashesForClusterService = readUnconfirmedTransactionsFromDB();
            }
            clusterService.setInitialUnconfirmedTransactions(hashesForClusterService);
            readConfirmedTransactionsFromDB();

            log.info("Balance service is up");
        } catch (Exception ex) {
            log.error("Errors on initiation ", ex);
        }
    }

    private void deleteConfirmedTransactions() {
        RocksIterator unconfirmedDBiterator = unconfirmedTransactions.getIterator();
        unconfirmedDBiterator.seekToFirst();
        while (unconfirmedDBiterator.isValid()) {
            ConfirmationData confirmationData = (ConfirmationData) SerializationUtils
                    .deserialize(unconfirmedDBiterator.value());
            if (confirmationData.isTrustChainConsensus() && confirmationData.isDoubleSpendPreventionConsensus()) {
                updateBalanceMap(confirmationData.getAddressHashToValueTransferredMapping(), balanceMap);
                publishBalanceChangeToWebSocket(confirmationData.getAddressHashToValueTransferredMapping().keySet());
                confirmedTransactions.put(confirmationData);
                unconfirmedTransactions.delete(confirmationData.getHash());
            }
            unconfirmedDBiterator.next();
        }
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will
     * continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelayString = "${balance.scheduled.delay}", initialDelayString = "${balance.scheduled.initialdelay}")
    private void updateBalanceFromQueueScheduledTask() {
        ConcurrentLinkedQueue<Hash> updateBalanceQueue = queueService.getUpdateBalanceQueue();
        while (!updateBalanceQueue.isEmpty()) {
            Hash addressHash = updateBalanceQueue.poll();
            ConfirmationData confirmationData = unconfirmedTransactions.getByHash(addressHash);
            log.info("confirmationData hash;{}", addressHash);
            //dspc = 1
            if (confirmationData.isDoubleSpendPreventionConsensus()) {
                ConfirmationData confirmedTransactionData = new ConfirmationData(transactions.getByHash(addressHash));
                confirmedTransactionData.setAddressHashToValueTransferredMapping(confirmationData.getAddressHashToValueTransferredMapping());
                confirmedTransactions.put(confirmedTransactionData);
                unconfirmedTransactions.delete(addressHash);
                updateBalanceMap(confirmationData.getAddressHashToValueTransferredMapping(), balanceMap);
                publishBalanceChangeToWebSocket(confirmationData.getAddressHashToValueTransferredMapping().keySet());
                liveViewService.updateNodeStatus(transactions.getByHash(addressHash), 2);

            } else { //dspc =0
                confirmationData.setTrustChainConsensus(true);
                setDSPCtoTrueAndInsertToUnconfirmed(confirmationData);
                log.info("The transaction {} was added to unconfirmedTransactions in the db and tcc was updated to true", confirmationData.getKey());
            }
        }
    }


    private void setDSPCtoTrueAndInsertToUnconfirmed(ConfirmationData confirmationData) {
        confirmationData.setDoubleSpendPreventionConsensus(true);
        unconfirmedTransactions.put(confirmationData);
    }


    private void updateBalanceMap(Map<Hash, BigDecimal> mapFrom, Map<Hash, BigDecimal> mapTo) {
        for (Map.Entry<Hash, BigDecimal> entry : mapFrom.entrySet()) {

            BigDecimal balance = entry.getValue();
            Hash key = entry.getKey();
            if (mapTo.containsKey(key)) {
                mapTo.put(key, balance.add(mapTo.get(key)));
            } else {
                mapTo.put(key, balance);
            }

            log.info("The address {} with the value {} was added to balance map and was removed from preBalanceMap", entry.getKey(), entry.getValue());
        }
    }

    private void readConfirmedTransactionsFromDB() {
        RocksIterator confirmedDBiterator = confirmedTransactions.getIterator();
        confirmedDBiterator.seekToFirst();
        while (confirmedDBiterator.isValid()) {
            ConfirmationData confirmedTransactionData = (ConfirmationData) SerializationUtils
                    .deserialize(confirmedDBiterator.value());
            updateBalanceMap(confirmedTransactionData.getAddressHashToValueTransferredMapping(), balanceMap);
            publishBalanceChangeToWebSocket(confirmedTransactionData.getAddressHashToValueTransferredMapping().keySet());
            confirmedDBiterator.next();

        }
    }

    private List<Hash> readUnconfirmedTransactionsFromDB() {
        List<Hash> hashesForClusterService = new LinkedList<>();

        RocksIterator unconfirmedDBiterator = unconfirmedTransactions.getIterator();
        unconfirmedDBiterator.seekToFirst();

        while (unconfirmedDBiterator.isValid()) {
            ConfirmationData confirmationData = (ConfirmationData) SerializationUtils
                    .deserialize(unconfirmedDBiterator.value());
            if (!confirmationData.isTrustChainConsensus()) {
                hashesForClusterService.add(confirmationData.getHash());
            }
            if (!confirmationData.isTrustChainConsensus() ||
                    !confirmationData.isDoubleSpendPreventionConsensus()) {
                updateBalanceMap(confirmationData.getAddressHashToValueTransferredMapping(), preBalanceMap);
            }
        }
        return hashesForClusterService;
    }

    private void loadBalanceFromSnapshot() {
        String snapshotFileLocation = "./Snapshot.csv";
        File snapshotFile = new File(snapshotFileLocation);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(snapshotFile))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] addressDetails = line.split(",");
                if (addressDetails.length != 2) {
                    throw new Exception("Bad csv file format");
                }
                Hash addressHash = new Hash(addressDetails[0]);
                BigDecimal addressAmount = new BigDecimal(addressDetails[1]);
                log.info("The hash {} was loaded from the snapshot with amount {}", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    // throw new Exception(String.format("Double address found in CSV file: %s", address));
                    log.error("The address {} was already found in the snapshot", addressHash);
                }
                balanceMap.put(addressHash, addressAmount);
                log.info("Loading from snapshot into inMem balance+preBalance address {} and amount {}",
                        addressHash, addressAmount);

            }
            // copy the balance to preBalance
            preBalanceMap.putAll(balanceMap);
        } catch (Exception e) {
            log.error("Errors on snapshot loading: {}", e);
        }
    }

    private List<Hash> generateGenesisTransactions() {
        List<Hash> unconfirmedTransactionHashes = new LinkedList<>();
        for (TransactionData transactionData : zeroSpendService.getGenesisTransactions()) {
            transactions.put(transactionData);
            ConfirmationData confirmationData = new ConfirmationData(transactionData);
            unconfirmedTransactions.put(confirmationData);
            unconfirmedTransactionHashes.add(confirmationData.getHash());
        }
        return unconfirmedTransactionHashes;
    }

    private void publishBalanceChangeToWebSocket(Set<Hash> addresses) {
        for (Hash address : addresses) {
            webSocketSender.notifyBalanceChange(address, balanceMap.get(address));
        }
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas) {
        try {
            for (BaseTransactionData baseTransactionData : baseTransactionDatas) {
                //checkBalance
                BigDecimal amount = baseTransactionData.getAmount();
                Hash addressHash = baseTransactionData.getAddressHash();
                if ((balanceMap.containsKey(addressHash) && amount.add(balanceMap.get(addressHash)).signum() < 0)
                        || (!balanceMap.containsKey(addressHash) && amount.signum() < 0)) {
                    log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                            amount, preBalanceMap.get(addressHash));
                    return false;
                }
                if (preBalanceMap.containsKey(addressHash)) {
                    if (amount.add(preBalanceMap.get(addressHash)).signum() < 0) {
                        log.error("Error in preBalance check. Address {}  amount {} current preBalance {} ", addressHash,
                                amount, preBalanceMap.get(addressHash));
                        return false;
                    } else {
                        preBalanceMap.put(addressHash, amount.add(preBalanceMap.get(addressHash)));
                    }
                } else {
                    if (amount.signum() < 0) {
                        log.error("Error in preBalance check. Address {}  amount {} current preBalance {} ", addressHash,
                                amount, preBalanceMap.get(addressHash));
                        return false;
                    }
                    preBalanceMap.put(addressHash, amount);
                }

            }
        } catch (Exception ex) {
            log.error("Exception on checking balances and adding to preBalance {}", ex);
        }
        return true;
    }

    @Override
    public void insertToUnconfirmedTransactions(ConfirmationData confirmationData) {
        setDSPCtoTrueAndInsertToUnconfirmed(confirmationData);

    }

    @Override
    public ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest) {
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse();
        List<AbstractMap.SimpleEntry<Hash, BigDecimal>> amounts = new LinkedList<>();
        for (Hash hash : getBalancesRequest.getAddresses()) {
            if (balanceMap.containsKey(hash)) {
                amounts.add(new AbstractMap.SimpleEntry<>(hash, balanceMap.get(hash)));
            } else {
                amounts.add(new AbstractMap.SimpleEntry<>(hash, new BigDecimal(-1)));
            }
        }
        getBalancesResponse.setAmounts(amounts);
        return ResponseEntity.status(HttpStatus.OK).body(getBalancesResponse);
    }


    public Map<Hash, BigDecimal> getBalanceMap() {
        return balanceMap;
    }

    public Map<Hash, BigDecimal> getPreBalanceMap() {
        return preBalanceMap;
    }

    @Override
    public void rollbackBaseTransactions(List<BaseTransactionData> baseTransactions) {
        for (BaseTransactionData baseTransactionData : baseTransactions) {
            if (preBalanceMap.containsKey(baseTransactionData.getAddressHash())) {
                baseTransactionData.setAmount(baseTransactionData.getAmount().negate());
            } else {
                // TODO : if not contains - can it happen ?
                log.error("Error while rolling back. preBalance map doesn't contain the address {}",
                        baseTransactionData.getAddressHash());
            }
        }
        if (!checkBalancesAndAddToPreBalance(baseTransactions)) {
            log.error("Error while rolling back. checkBalancesAndAddToPreBalance returned false");
        }
    }

}
