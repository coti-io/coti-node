package io.coti.historynode.services;

import io.coti.basenode.services.BaseNodeBalanceService;
import org.springframework.stereotype.Service;

@Service
public class BalanceService extends BaseNodeBalanceService {

    @Override
    public void init() throws Exception {
        // TODO: blank method, for start-up checks, to be removed later
    }

    @Override
    public void validateBalances() {
        // TODO: blank method, for start-up checks, to be removed later
    }

}
