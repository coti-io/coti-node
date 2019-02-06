package io.coti.zerospend.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class BalanceService extends BaseNodeBalanceService {

    public Map<Hash, BigDecimal> getBalanceMap() {
        return balanceMap;
    }
}
