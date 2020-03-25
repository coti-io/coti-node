package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeRegistrationData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyTypeRegistrationCrypto extends SignatureCrypto<CurrencyTypeRegistrationData> {
// todo delete it
    @Override
    public byte[] getSignatureMessage(CurrencyTypeRegistrationData currencyTypeRegistrationData) {
        byte[] currencyHashInBytes = currencyTypeRegistrationData.getCurrencyHash().getBytes();
        byte[] currencyTypeInBytes = currencyTypeRegistrationData.getCurrencyType().getText().getBytes();
//        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyTypeRegistrationData.getCreationTime().toEpochMilli()).array();
        ByteBuffer currencyTypeRegistrationBuffer = ByteBuffer.allocate(currencyHashInBytes.length + currencyTypeInBytes.length) //+ creationTimeInBytes.length)
                .put(currencyHashInBytes).put(currencyTypeInBytes); //.put(creationTimeInBytes);
        return CryptoHelper.cryptoHash(currencyTypeRegistrationBuffer.array()).getBytes();
    }
}
