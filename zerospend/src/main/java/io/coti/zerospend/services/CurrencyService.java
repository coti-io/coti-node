package io.coti.zerospend.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            initTestNativeCurrencyEntry();
            initTestNonNativeCurrencyEntries();
        }
    }

}
