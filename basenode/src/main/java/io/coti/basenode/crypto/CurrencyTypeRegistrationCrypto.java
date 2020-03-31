package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeRegistrationData;
import org.springframework.stereotype.Service;

@Service
public class CurrencyTypeRegistrationCrypto extends SignatureCrypto<CurrencyTypeRegistrationData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeRegistrationData currencyTypeRegistrationData) {
        return CryptoHelper.cryptoHash(currencyTypeRegistrationData.getMessageInBytes()).getBytes();
    }
}
