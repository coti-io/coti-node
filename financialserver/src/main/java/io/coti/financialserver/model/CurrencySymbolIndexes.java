package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.CurrencySymbolIndexData;
import org.springframework.stereotype.Service;

@Service
public class CurrencySymbolIndexes extends Collection<CurrencySymbolIndexData> {

    public void init() {
        super.init();
    }

}
