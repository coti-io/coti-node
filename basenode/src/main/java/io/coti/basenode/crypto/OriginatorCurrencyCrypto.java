package io.coti.basenode.crypto;

import io.coti.basenode.data.OriginatorCurrencyData;
import org.springframework.stereotype.Service;

@Service
public class OriginatorCurrencyCrypto extends SignatureCrypto<OriginatorCurrencyData> {

    @Override
    public byte[] getSignatureMessage(OriginatorCurrencyData currencyData) {
        return CryptoHelper.cryptoHash(currencyData.getMessageInBytes()).getBytes();
    }

}
