package io.coti.financialserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CurrencyRateService {

    public double getTokenRateToNativeCoin() {
        return 1.0;
    }
}