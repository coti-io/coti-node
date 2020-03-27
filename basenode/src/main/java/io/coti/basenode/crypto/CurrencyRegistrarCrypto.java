package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyRegistrarCrypto extends SignatureCrypto<CurrencyData> {
//todo delete it
    @Override
    public byte[] getSignatureMessage(CurrencyData currencyData) {
        byte[] hashInBytes = currencyData.getHash().getBytes();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyData.getCreateTime().toEpochMilli()).array();
        ByteBuffer currencyBuffer = ByteBuffer.allocate(hashInBytes.length + creationTimeInBytes.length)
                .put(hashInBytes).put(creationTimeInBytes);
        return CryptoHelper.cryptoHash(currencyBuffer.array()).getBytes();
    }
}
