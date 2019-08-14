package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetCurrenciesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

@Service
public class GetCurrenciesResponseCrypto extends SignatureCrypto<GetCurrenciesResponse> {

    @Override
    public byte[] getSignatureMessage(GetCurrenciesResponse getCurrenciesResponse) {

        byte[] signerHashInBytes = getCurrenciesResponse.getSignerHash().getBytes();
        Set<Hash> currenciesHashes = new HashSet<>();
        getCurrenciesResponse.getCurrencyDataSet().forEach(currencyData -> {
            currenciesHashes.add(currencyData.getHash());
        });
        byte[] currencyHashesInBytes = new byte[0];
        if (!currenciesHashes.isEmpty()) {
            currencyHashesInBytes = getCurrencyHashesInBytes(currenciesHashes);
        }

        ByteBuffer buffer = ByteBuffer.allocate(signerHashInBytes.length + currencyHashesInBytes.length);
        buffer.put(signerHashInBytes).put(currencyHashesInBytes);

        return CryptoHelper.cryptoHash(buffer.array()).getBytes();
    }

    private byte[] getCurrencyHashesInBytes(Set<Hash> currenciesHashes) {
        ByteBuffer currencyHashesBuffer =
                ByteBuffer.allocate(currenciesHashes.size() * currenciesHashes.iterator().next().getBytes().length);
        currenciesHashes.forEach(currencyHash -> {
            currencyHashesBuffer.put(currencyHash.getBytes());
        });
        return currencyHashesBuffer.array();
    }
}
