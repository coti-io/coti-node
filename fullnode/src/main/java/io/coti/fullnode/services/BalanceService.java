package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.fullnode.websocket.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BalanceService extends BaseNodeBalanceService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    public void continueHandleBalanceChanges(Hash addressHash, Hash currencyHash) {
        webSocketSender.notifyBalanceChange(addressHash, getNativeCurrencyHashIfNull(currencyHash), getBalance(addressHash, currencyHash), getPreBalance(addressHash, currencyHash));
    }
}
