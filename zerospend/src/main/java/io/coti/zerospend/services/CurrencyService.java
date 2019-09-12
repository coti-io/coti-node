package io.coti.zerospend.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    @Value("${native.token.name}")
    private String nativeTokenName;
    @Value("${native.token.symbol}")
    private String nativeTokenSymbol;
    @Value("${native.token.supply}")
    private BigDecimal nativeTokenTotalSupply;
    @Value("${native.token.scale}")
    private int nativeTokenScale;
    @Value("${native.token.description}")
    private String nativeTokenDescription;
    @Value("${native.token.genesis.address}")
    private String nativeTokenAddress;

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            generateNativeToken();
        }
    }

    private void generateNativeToken() {
        CurrencyData currencyData = super.createCurrencyData(nativeTokenName, (nativeTokenSymbol).toUpperCase(), nativeTokenTotalSupply, nativeTokenScale, Instant.now(), nativeTokenDescription
                , CurrencyType.NATIVE_COIN);
        putCurrencyData(currencyData);
        setNativeCurrencyData(currencyData);
    }

}