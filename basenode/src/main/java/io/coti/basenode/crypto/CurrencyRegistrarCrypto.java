package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;

import java.nio.ByteBuffer;

public class CurrencyRegistrarCrypto extends SignatureCrypto<CurrencyData> {
    @Override
    public byte[] getSignatureMessage(CurrencyData currencyData) {
        byte[] hashInBytes = currencyData.getHash().getBytes();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyData.getCreationTime().toEpochMilli()).array();
        ByteBuffer currencyBuffer = ByteBuffer.allocate(hashInBytes.length + creationTimeInBytes.length)
                .put(hashInBytes).put(creationTimeInBytes);
        return CryptoHelper.cryptoHash(currencyBuffer.array()).getBytes();
    }
}
