package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.CurrencyNameIndexData;
import org.springframework.stereotype.Service;

@Service
public class CurrencyNameIndexes extends Collection<CurrencyNameIndexData> {

    public void init() {
        super.init();
    }

}
