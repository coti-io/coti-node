package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyTypeCrypto extends SignatureCrypto<CurrencyTypeData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeData currencyTypeData) {
        byte[] currencyTypeInBytes = currencyTypeData.getCurrencyType().getText().getBytes();
        long creationTime = currencyTypeData.getCreationTime().toEpochMilli();

        ByteBuffer buffer = ByteBuffer.allocate(currencyTypeInBytes.length + Long.BYTES);
        buffer.put(currencyTypeInBytes).putLong(creationTime);

        return CryptoHelper.cryptoHash(buffer.array()).getBytes();
    }
}
