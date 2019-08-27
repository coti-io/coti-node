package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetCurrencyResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

@Service
public class GetCurrencyResponseCrypto extends SignatureCrypto<GetCurrencyResponse> {

    @Override
    public byte[] getSignatureMessage(GetCurrencyResponse getCurrencyResponse) {
        Set<Hash> currenciesHashes = new HashSet<>();
        getCurrencyResponse.getCurrencyDataSet().forEach(currencyData -> {
            currenciesHashes.add(currencyData.getHash());
        });
        byte[] currencyHashesInBytes = new byte[0];
        ByteBuffer currencyHashesBuffer;
        if (!currenciesHashes.isEmpty()) {
            currencyHashesBuffer =
                    ByteBuffer.allocate(currenciesHashes.size() * currenciesHashes.iterator().next().getBytes().length);
            currenciesHashes.forEach(currencyHash -> {
                currencyHashesBuffer.put(currencyHash.getBytes());
            });
            currencyHashesInBytes = currencyHashesBuffer.array();
        }
        return CryptoHelper.cryptoHash(currencyHashesInBytes).getBytes();
    }

}
