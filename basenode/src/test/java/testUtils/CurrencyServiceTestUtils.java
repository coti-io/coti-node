package testUtils;

import io.coti.basenode.crypto.CurrencyCrypto;
import io.coti.basenode.crypto.CurrencyTypeCrypto;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
public class CurrencyServiceTestUtils {
    @Autowired
    private CurrencyTypeCrypto currencyTypeCrypto;
    @Autowired
    private CurrencyCrypto currencyCrypto;

    public static CurrencyData createCurrencyData(String name, String symbol, Hash hash) {
        CurrencyData currencyData = new CurrencyData();
        currencyData.setName(name);
        currencyData.setSymbol(symbol);
        currencyData.setHash(hash);
        currencyData.setTotalSupply(new BigDecimal(700000));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
        currencyData.setDescription("tempDescription");
        currencyData.setRegistrarHash(new Hash("tempRegistrar"));
        currencyData.setSignerHash(new Hash("tempSigner"));
        currencyData.setOriginatorHash(new Hash("tempOriginator"));
//        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now(), null);
//        currencyTypeCrypto.signMessage(currencyTypeData);
//        currencyData.setCurrencyTypeData(currencyTypeData);
//        currencyCrypto.signMessage(currencyData);
        return currencyData;
    }
}
