package io.coti.financialserver.services;

import io.coti.basenode.data.CurrencyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CurrencyRateService {

    public double getTokenRateToNativeCoin(CurrencyData currencyHash) {
        return 1.0;
    }
}