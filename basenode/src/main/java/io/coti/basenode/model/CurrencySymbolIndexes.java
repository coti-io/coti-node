package io.coti.basenode.model;

import io.coti.basenode.data.CurrencySymbolIndexData;
import org.springframework.stereotype.Service;

@Service
public class CurrencySymbolIndexes  extends Collection<CurrencySymbolIndexData> {

    public void init() {
        super.init();
    }

}
