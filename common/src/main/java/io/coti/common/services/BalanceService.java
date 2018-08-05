package io.coti.common.services;

import io.coti.common.data.*;
import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.model.*;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import io.coti.common.services.interfaces.IQueueService;
import io.coti.common.services.interfaces.IZeroSpendService;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class BalanceService implements IBalanceService {


    @Autowired
    private Addresses addresses;


    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;

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
    private void init() throws Exception {
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        loadBalanceFromSnapshot();
//            deleteConfirmedTransactions();
        // TODO: call RestTemplatePropagation.propagateMultiTransactionFromNeighbor (with no parameter of fromAttachmentTimeStampIfThere AreNoTransactions)
        // Then set all transactions on TCC=0, sort them topological, and  call for  every transaction to TransactionService.addPropagatedTransaction
        List<Hash> hashesForClusterService;
        if (unconfirmedTransactions.isEmpty()) {
            throw new Exception("Database is empty! please use --resetData=true");
        } else {
            hashesForClusterService = readUnconfirmedTransactionsFromDB();
        }
        clusterService.setInitialUnconfirmedTransactions(hashesForClusterService);
        readConfirmedTransactionsFromDB();

        log.info("Balance service is up");
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will
     * continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelayString = "${balance.scheduled.delay}", initialDelayString = "${balance.scheduled.initialdelay}")
    private void updateBalanceFromQueueScheduledTask() {
        ConcurrentLinkedQueue<TccInfo> updateBalanceQueue = queueService.getUpdateBalanceQueue();
        while (!updateBalanceQueue.isEmpty()) {
            TccInfo tccInfo = updateBalanceQueue.poll();
            ConfirmationData confirmationData = unconfirmedTransactions.getByHash(tccInfo.getHash());
            log.info("confirmationData hash;{}", tccInfo.getHash());
            //dspc = 1
            TransactionData currentTransactionData = confirmedTransactions.putConfirmedAndUpdateTransaction(confirmationData, tccInfo);
            if (confirmationData.isDoubleSpendPreventionConsensus()) {
                unconfirmedTransactions.delete(confirmationData.getHash());
                updateBalanceMap(confirmationData.getAddressHashToValueTransferredMapping(), balanceMap);
                publishBalanceChangeToWebSocket(confirmationData.getAddressHashToValueTransferredMapping().keySet());
                webSocketSender.notifyTransactionHistoryChange(currentTransactionData, TransactionStatus.TCC_APPROVED);
                liveViewService.updateNodeStatus(currentTransactionData, 2);

            } else { //dspc =0
                confirmationData.setTrustChainConsensus(true);
                setDSPCtoTrueAndInsertToUnconfirmed(confirmationData);
                webSocketSender.notifyTransactionHistoryChange(currentTransactionData, TransactionStatus.TCC_APPROVED);
                log.info("The transaction {} was added to unconfirmedTransactions in the db and tcc was updated to true", confirmationData.getHash());
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

            log.debug("The address {} with the value {} was added to balance map and was removed from preBalanceMap", entry.getKey(), entry.getValue());
        }
    }

    private void readConfirmedTransactionsFromDB() {
        RocksIterator confirmedDBiterator = confirmedTransactions.getIterator();
        confirmedDBiterator.seekToFirst();
        while (confirmedDBiterator.isValid()) {
            ConfirmationData confirmedTransactionData = (ConfirmationData) SerializationUtils
                    .deserialize(confirmedDBiterator.value());
            confirmedTransactionData.setHash(new Hash(confirmedDBiterator.key()));
            updateBalanceMap(confirmedTransactionData.getAddressHashToValueTransferredMapping(), balanceMap);
            liveViewService.addNode(transactions.getByHash(confirmedTransactionData.getHash()));
            confirmedDBiterator.next();
        }
    }

    private List<Hash> readUnconfirmedTransactionsFromDB() {
        List<Hash> hashesForClusterService = new LinkedList<>();

        RocksIterator unconfirmedDBIterator = unconfirmedTransactions.getIterator();
        unconfirmedDBIterator.seekToFirst();

        while (unconfirmedDBIterator.isValid()) {
            ConfirmationData confirmationData = (ConfirmationData) SerializationUtils
                    .deserialize(unconfirmedDBIterator.value());
            confirmationData.setHash(new Hash(unconfirmedDBIterator.key()));
            if (!confirmationData.isTrustChainConsensus()) {
                hashesForClusterService.add(confirmationData.getHash());
            }
            if (!confirmationData.isTrustChainConsensus() ||
                    !confirmationData.isDoubleSpendPreventionConsensus()) {
                updateBalanceMap(confirmationData.getAddressHashToValueTransferredMapping(), preBalanceMap);
            }
            liveViewService.addNode(transactions.getByHash(confirmationData.getHash()));
            unconfirmedDBIterator.next();
        }
        return hashesForClusterService;
    }

    private void loadBalanceFromSnapshot() throws Exception {
        String snapshotFileLocation = "snapshot.csv";
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
                log.debug("The hash {} was loaded from the snapshot with amount {}", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    log.error("The address {} was already found in the snapshot", addressHash);
                    throw new Exception(String.format("The address %s was already found in the snapshot", addressHash));
                }
                balanceMap.put(addressHash, addressAmount);
                log.debug("Loading from snapshot into inMem balance+preBalance address {} and amount {}",
                        addressHash, addressAmount);
            }
            log.info("Snapshot is finished");
            preBalanceMap.putAll(balanceMap);
        } catch (Exception e) {
            log.error("Errors on snapshot loading: {}", e);
            throw e;
        }
    }

    private void publishBalanceChangeToWebSocket(Set<Hash> addresses) {
        for (Hash address : addresses) {
            publishBalanceChangeToWebSocket(address);
        }
    }

    private void publishBalanceChangeToWebSocket(Hash address) {
            webSocketSender.notifyBalanceChange(address, balanceMap.get(address),preBalanceMap.get(address) );
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas) {
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
                    publishBalanceChangeToWebSocket(addressHash);
                }
            } else {
                if (amount.signum() < 0) {
                    log.error("Error in preBalance check. Address {}  amount {} current preBalance {} ", addressHash,
                            amount, preBalanceMap.get(addressHash));
                    return false;
                }
                preBalanceMap.put(addressHash, amount);
                publishBalanceChangeToWebSocket(addressHash);
            }
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
        for (Hash hash : getBalancesRequest.getAddresses()) {
            if (balanceMap.containsKey(hash)) {
                getBalancesResponse.addAddressBalanceToResponse(hash, balanceMap.get(hash), preBalanceMap.get(hash));
            } else {
                getBalancesResponse.addAddressBalanceToResponse(hash, new BigDecimal(0),new BigDecimal(0));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(getBalancesResponse);
    }

    public Map<Hash, BigDecimal> getBalanceMap() {
        return balanceMap;
    }

    public Map<Hash, BigDecimal> getPreBalanceMap() {
        return preBalanceMap;
    }

    @Override
    public void rollbackBaseTransactions(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            baseTransactionData.setAmount(baseTransactionData.getAmount().negate());
            preBalanceMap.replace(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
        }
    }
}
