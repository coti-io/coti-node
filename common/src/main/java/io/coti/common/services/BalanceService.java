package io.coti.common.services;

import io.coti.common.data.*;
import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;

    private Map<Hash, BigDecimal> balanceMap;
    private Map<Hash, BigDecimal> preBalanceMap;
    private BlockingQueue<ConfirmationData> confirmationQueue;

    @PostConstruct
    private void init() throws Exception {
        confirmationQueue = new LinkedBlockingQueue<>();
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        loadBalanceFromSnapshot();
        new Thread(() -> updateConfirmedTransactions()).start();
    }

    private void updateConfirmedTransactions() {
        while (true) {
            try {
                ConfirmationData confirmationData = confirmationQueue.take();
                TransactionData transactionData = transactions.getByHash(confirmationData.getHash());
                if (confirmationData instanceof TccInfo) {
                    transactionData.setTrustChainConsensus(true);
                    transactionData.setTrustChainTrustScore(((TccInfo) confirmationData).getTrustChainTrustScore());
                    transactionData.setTrustChainTransactionHashes(((TccInfo) confirmationData).getTrustChainTransactionHashes());
                    if (transactionData.isZeroSpend() || transactionData.getDspConsensusResult() != null)
                        processConfirmedTransaction(transactionData);
                } else if (confirmationData instanceof DspConsensusResult) {
                    transactionData.setDspConsensusResult((DspConsensusResult) confirmationData);
                    if (transactionData.isTrustChainConsensus())
                        processConfirmedTransaction(transactionData);
                }
                transactions.put(transactionData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processConfirmedTransaction(TransactionData transactionData) {
        transactionData.setTransactionConsensusUpdateTime(new Date());
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            balanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (hash, currentAmount) -> currentAmount.add(baseTransactionData.getAmount()));
            balanceMap.putIfAbsent(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
        });
        publishBalanceChangeToWebSocket(
                transactionData.getBaseTransactions()
                        .stream()
                        .map(BaseTransactionData::getAddressHash)
                        .collect(Collectors.toSet()));
        liveViewService.updateNodeStatus(transactionData, 2);
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.CONFIRMED);
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
        webSocketSender.notifyBalanceChange(address, balanceMap.get(address), preBalanceMap.get(address));
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas) {
        Map<Hash, BigDecimal> preBalanceChanges = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactionDatas) {

            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            BigDecimal balance = balanceMap.containsKey(addressHash) ? balanceMap.get(addressHash) : BigDecimal.ZERO;
            BigDecimal preBalance = preBalanceMap.containsKey(addressHash) ? preBalanceMap.get(addressHash) : BigDecimal.ZERO;
            if (amount.add(balance).signum() < 0) {
                log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                        amount, balance);
                return false;
            }
            if (amount.add(preBalance).signum() < 0) {
                log.error("Error in PreBalance check. Address {}  amount {} current PreBalance {} ", addressHash,
                        amount, preBalance);
                return false;
            }
            preBalanceChanges.put(addressHash, amount.add(preBalance));
        }
        preBalanceChanges.forEach((addressHash, preBalance) -> {
            preBalanceMap.put(addressHash, preBalance);
            publishBalanceChangeToWebSocket(addressHash);
        });
        return true;

    }


    @Override
    public ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest) {
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse();
        for (Hash hash : getBalancesRequest.getAddresses()) {
            if (balanceMap.containsKey(hash)) {
                getBalancesResponse.addAddressBalanceToResponse(hash, balanceMap.get(hash), preBalanceMap.get(hash));
            } else {
                getBalancesResponse.addAddressBalanceToResponse(hash, new BigDecimal(0), new BigDecimal(0));
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
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                preBalanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (addressHash, amount) -> amount.add(baseTransactionData.getAmount().negate()))
        );
    }

    @Override
    public void insertSavedTransaction(TransactionData transactionData) {
        boolean isConfirmed = transactionHelper.isConfirmed(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            preBalanceMap.putIfAbsent(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
            preBalanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (currentHash, currentAmount) ->
                    currentAmount.add(baseTransactionData.getAmount()));
            if (isConfirmed) {
                balanceMap.putIfAbsent(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
                balanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (currentHash, currentAmount) ->
                        currentAmount.add(baseTransactionData.getAmount()));
            }
        });
    }

    @Override
    public void finalizeInit() {
        validateBalances();
        log.info("Balance Service is up");
    }

    private void validateBalances() {
        preBalanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("PreBalance Validation failed!");
                throw new IllegalArgumentException("Snapshot or database are corrupted.");
            }
        });
        balanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("Balance Validation failed!");
                throw new IllegalArgumentException("Snapshot or database are corrupted.");
            }
        });
        log.info("Balance Validation completed");
    }

    @Override
    public void setTccToTrue(TccInfo tccInfo) {
        try {
            confirmationQueue.put(tccInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDspcToTrue(DspConsensusResult dspConsensusResult) {
        try {
            confirmationQueue.put(dspConsensusResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}