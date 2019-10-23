package io.coti.zerospend.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Currencies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Value("${reset.currencies: false}")
    private boolean resetCurrencies;

    protected boolean reset() {
        boolean reset = super.reset();
        resetColumnFamilyNames.addAll(Arrays.asList(
                Currencies.class.getName()
        ));
        return reset || resetCurrencies;
    }
}
