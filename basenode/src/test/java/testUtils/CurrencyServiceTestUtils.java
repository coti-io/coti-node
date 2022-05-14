package testUtils;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.interfaces.ICurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
public class CurrencyServiceTestUtils {

    @Autowired
    private static ICurrencyService currencyService;

    public static CurrencyData createCurrencyData(String name, String symbol, Hash hash) {
        CurrencyData currencyData = new CurrencyData();
        currencyData.setName(name);
        currencyData.setSymbol(symbol);
        currencyData.setHash(hash);
        currencyData.setTotalSupply(new BigDecimal("700000"));
        currencyData.setScale(8);
        currencyData.setCreateTime(Instant.now());
        currencyData.setDescription("tempDescription");
        currencyData.setSignerHash(new Hash("tempSigner"));
        currencyData.setOriginatorHash(new Hash("tempOriginator"));
        return currencyData;
    }
}
