package io.coti.financialserver.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.exceptions.CurrencyInitializationException;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.model.CurrencySymbolIndexes;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.data.CurrencySymbolIndexData;
import io.coti.financialserver.data.CurrencyNameIndexData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private CurrencySymbolIndexes currencySymbolIndexes;
    @Autowired
    protected INetworkService networkService;

    @Override
    public void updateCurrencyDataIndexes(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
        currencySymbolIndexes.put(new CurrencySymbolIndexData(currencyData.getSymbol(), currencyData.getHash()));
    }

    @Override
    public void removeCurrencyDataIndexes(CurrencyData currencyData) {
        currencyNameIndexes.delete(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
        currencySymbolIndexes.delete(new CurrencySymbolIndexData(currencyData.getSymbol(), currencyData.getHash()));
    }

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            String recoveryServerAddress = networkService.getRecoveryServerAddress();
            RestTemplate restTemplate = new RestTemplate();
            nativeCurrencyData = restTemplate.getForObject(recoveryServerAddress + GET_NATIVE_CURRENCY_ENDPOINT, CurrencyData.class);
        }
        if (nativeCurrencyData == null) {
            throw new CurrencyInitializationException("Failed to retrieve native currency");
        } else {
            putCurrencyData(nativeCurrencyData);
            setNativeCurrencyData(nativeCurrencyData);
        }
    }

}