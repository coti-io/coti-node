package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static io.coti.fullnode.services.NodeServiceManager.currencyService;
import static io.coti.fullnode.services.NodeServiceManager.webSocketSender;

@Service
@Primary
public class BalanceService extends BaseNodeBalanceService {

    @Override
    public void continueHandleBalanceChanges(Hash addressHash, Hash currencyHash) {
        webSocketSender.notifyBalanceChange(addressHash, currencyService.getNativeCurrencyHashIfNull(currencyHash), getBalance(addressHash, currencyHash), getPreBalance(addressHash, currencyHash));
    }
}
