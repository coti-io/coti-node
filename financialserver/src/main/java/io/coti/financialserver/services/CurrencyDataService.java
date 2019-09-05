package io.coti.financialserver.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.financialserver.data.CurrencyNameIndexData;
import io.coti.financialserver.data.CurrencySymbolIndexData;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.CurrencySymbolIndexes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CurrencyDataService extends BaseNodeCurrencyService {

    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private CurrencySymbolIndexes currencySymbolIndexes;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());

        super.init();
    }

    @Override
    protected void updateCurrencyDataIndexes(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
        currencySymbolIndexes.put(new CurrencySymbolIndexData(currencyData.getSymbol(), currencyData.getHash()));
    }

    @Override
    protected void removeCurrencyDataIndexes(CurrencyData currencyData) {
        currencyNameIndexes.delete(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
        currencySymbolIndexes.delete(new CurrencySymbolIndexData(currencyData.getSymbol(), currencyData.getHash()));
    }


}
