package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.IUnconfirmedReceivedTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeUnconfirmedReceivedTransactionService implements IUnconfirmedReceivedTransactionService {

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

}
