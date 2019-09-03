package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.http.GetUpdatedCurrencyResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;

@Service
public class GetUpdatedCurrencyResponseCrypto extends SignatureCrypto<GetUpdatedCurrencyResponse> {
    @Override
    public byte[] getSignatureMessage(GetUpdatedCurrencyResponse getUpdatedCurrencyResponse) {
        final Map<CurrencyType, HashSet<CurrencyData>> currencyDataByType = getUpdatedCurrencyResponse.getCurrencyDataByType();
        int notEmptyCurrencyTypeLength = 0;
        int numberOfCurrencyHashes = 0;
        int currencyHashSize = 0;

        for (Map.Entry<CurrencyType, HashSet<CurrencyData>> entry : currencyDataByType.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                notEmptyCurrencyTypeLength += entry.getKey().getText().getBytes().length;
                numberOfCurrencyHashes += entry.getValue().size();
                if (currencyHashSize == 0) {
                    currencyHashSize = entry.getValue().iterator().next().getHash().getBytes().length;
                }
            }
        }

        ByteBuffer currencyMapBuffer = ByteBuffer.allocate(notEmptyCurrencyTypeLength + numberOfCurrencyHashes * currencyHashSize);

        for (Map.Entry<CurrencyType, HashSet<CurrencyData>> entry : currencyDataByType.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                currencyMapBuffer.put(entry.getKey().getText().getBytes());
                entry.getValue().forEach(currencyData -> {
                    currencyMapBuffer.put(currencyData.getHash().getBytes());
                });
            }
        }

        return CryptoHelper.cryptoHash(currencyMapBuffer.array()).getBytes();
    }
}
