package testUtils;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.Instant;

@Slf4j
public class CurrencyServiceTestUtils {

    public static CurrencyData createCurrencyData(String name, String symbol, Hash hash) {
        CurrencyData currencyData = new CurrencyData();
        currencyData.setName(name);
        currencyData.setSymbol(symbol);
        currencyData.setHash(hash);
        currencyData.setTotalSupply(new BigInteger("700000"));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
        currencyData.setDescription("tempDescription");
        currencyData.setRegistrarHash(new Hash("tempRegistrar"));
        currencyData.setSignerHash(new Hash("tempSigner"));
        currencyData.setOriginatorHash(new Hash("tempOriginator"));
        return currencyData;
    }
}
