package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;

@Service
public class GetUpdatedCurrencyRequestCrypto extends SignatureCrypto<GetUpdatedCurrencyRequest> {

    @Override
    public byte[] getSignatureMessage(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        final Map<CurrencyType, HashSet<Hash>> currencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
        int notEmptyCurrencyTypeLength = 0;
        int numberOfCurrencyHashes = 0;
        int currencyHashSize = 0;

        for (Map.Entry<CurrencyType, HashSet<Hash>> entry : currencyHashesByType.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                notEmptyCurrencyTypeLength += entry.getKey().getText().getBytes().length;
                numberOfCurrencyHashes += entry.getValue().size();
                if (currencyHashSize == 0) {
                    currencyHashSize = entry.getValue().iterator().next().getBytes().length;
                }
            }
        }
        ByteBuffer currencyMapBuffer = ByteBuffer.allocate(notEmptyCurrencyTypeLength + numberOfCurrencyHashes * currencyHashSize);

        for (Map.Entry<CurrencyType, HashSet<Hash>> entry : currencyHashesByType.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                currencyMapBuffer.put(entry.getKey().getText().getBytes());
                entry.getValue().forEach(currencyHash -> {
                    currencyMapBuffer.put(currencyHash.getBytes());
                });
            }
        }

        return CryptoHelper.cryptoHash(currencyMapBuffer.array()).getBytes();
    }
}
