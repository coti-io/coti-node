package io.coti.zerospend.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    @Value("${native.currency.name}")
    private String nativeCurrencyName;
    @Value("${native.currency.symbol}")
    private String nativeCurrencySymbol;
    @Value("${native.currency.supply}")
    private BigDecimal nativeCurrencyTotalSupply;
    @Value("${native.currency.scale}")
    private int nativeCurrencyScale;
    @Value("${native.currency.description}")
    private String nativeCurrencyDescription;
    @Value("${native.currency.genesis.address}")
    private String nativeCurrencyAddress;

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            generateNativeCurrency();
        }
    }

    private void generateNativeCurrency() {
        CurrencyData currencyData = super.createCurrencyData(nativeCurrencyName, (nativeCurrencySymbol).toUpperCase(), nativeCurrencyTotalSupply, nativeCurrencyScale, Instant.now(), nativeCurrencyDescription
                , CurrencyType.NATIVE_COIN);
        putCurrencyData(currencyData);
        setNativeCurrencyData(currencyData);
    }

}