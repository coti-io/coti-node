package io.coti.zerospend.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private ClusterStampService clusterStampService;

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            generateNativeCurrency();
        }
    }

    private void generateNativeCurrency() {
        CurrencyData currencyData = new CurrencyData();
        Instant creationTime = Instant.now();
        currencyData.setName(nativeCurrencyName);
        currencyData.setSymbol(nativeCurrencySymbol.toUpperCase());
        currencyData.setTotalSupply(nativeCurrencyTotalSupply);
        currencyData.setScale(nativeCurrencyScale);
        currencyData.setCreateTime(creationTime);
        currencyData.setDescription(nativeCurrencyDescription);
        CurrencyTypeData currencyTypeData = new CurrencyTypeData(CurrencyType.NATIVE_COIN, creationTime);
        currencyTypeData.setSymbol(currencyData.getSymbol());
        currencyData.setCurrencyTypeData(currencyTypeData);
        currencyData.setHash();

        currencyTypeCrypto.signMessage(currencyData.getCurrencyTypeData());
        originatorCurrencyCrypto.signMessage(currencyData);

        putCurrencyData(currencyData);
        setNativeCurrencyData(currencyData);
    }
}