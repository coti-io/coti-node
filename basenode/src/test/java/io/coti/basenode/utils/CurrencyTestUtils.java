package io.coti.basenode.utils;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.Hash;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
public class CurrencyTestUtils {
    public static CurrencyData createCurrencyData(String name, String symbol, Hash hash) {
        return createCurrencyData(name, symbol);
    }

    public static CurrencyData createCurrencyData(String name, String symbol) {
        return createCurrencyData(name, symbol, CurrencyType.NATIVE_COIN);

    }

    public static CurrencyData createCurrencyData(String name, String symbol, CurrencyType currencyType) {
        CurrencyData currencyData = new CurrencyData();
        currencyData.setName(name);
        currencyData.setSymbol(symbol);
        currencyData.setHash();
        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
        currencyData.setCurrencyTypeData(currencyTypeData);
        currencyData.setTotalSupply(new BigDecimal("700000"));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
        currencyData.setDescription("tempDescription");
        return currencyData;
    }
}
