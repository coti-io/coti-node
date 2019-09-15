package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.fullnode.websocket.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.coti.basenode.services.CurrencyService.NATIVE_CURRENCY_HASH;


@Service
public class BalanceService extends BaseNodeBalanceService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    public void continueHandleBalanceChanges(Hash addressHash) {
        webSocketSender.notifyBalanceChange(addressHash, balanceMap.get(addressHash).get(NATIVE_CURRENCY_HASH), preBalanceMap.get(addressHash).get(NATIVE_CURRENCY_HASH));
    }
}
