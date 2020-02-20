package io.coti.fullnode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BalanceService extends BaseNodeBalanceService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    public void continueHandleBalanceChanges(Hash addressHash) {
        webSocketSender.notifyBalanceChange(addressHash, balanceMap.get(addressHash), preBalanceMap.get(addressHash));
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions) {
        Map<Hash, BigDecimal> balanceInChangeMap = new HashMap<>();
        Map<Hash, BigDecimal> preBalanceInChangeMap = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactions) {

            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            balanceInChangeMap.putIfAbsent(addressHash, balanceMap.getOrDefault(addressHash, BigDecimal.ZERO));
            preBalanceInChangeMap.putIfAbsent(addressHash, preBalanceMap.getOrDefault(addressHash, BigDecimal.ZERO));
            if (amount.add(balanceInChangeMap.get(addressHash)).signum() < 0) {
                log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                        amount, balanceInChangeMap.get(addressHash));
                return false;
            }
            if (amount.add(preBalanceInChangeMap.get(addressHash)).signum() < 0) {
                log.error("Error in PreBalance check. Address {}  amount {} current PreBalance {} ", addressHash,
                        amount, preBalanceInChangeMap.get(addressHash));
                return false;
            }
            preBalanceInChangeMap.put(addressHash, amount.add(preBalanceInChangeMap.get(addressHash)));
        }
        preBalanceInChangeMap.forEach((addressHash, preBalanceInChange) -> {
            preBalanceMap.put(addressHash, preBalanceInChange);
            continueHandleBalanceChanges(addressHash);
        });
        return true;
    }

    @Override
    public void validateBalancesOnInit() {
        preBalanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("PreBalance Validation failed!");
                throw new IllegalArgumentException("ClusterStamp or database are corrupted.");
            }
        });
        balanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("Balance Validation failed!");
                throw new IllegalArgumentException("ClusterStamp or database are corrupted.");
            }
        });
        log.info("Balance Validation completed");
    }

}
