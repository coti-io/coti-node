package io.coti.zerospend.database;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Currencies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Value("${reset.currencies: false}")
    private boolean resetCurrencies;

    @Override
    protected void populateResetColumnFamilyNames() {
        if (resetCurrencies) {
            resetColumnFamilyNames.addAll(Collections.singletonList(
                    Currencies.class.getName()
            ));
        }
    }
}
