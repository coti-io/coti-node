package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyCrypto extends SignatureCrypto<CurrencyData> {

    @Override
    public byte[] getSignatureMessage(CurrencyData currencyData) {
        byte[] hashInBytes = currencyData.getHash().getBytes();
        byte[] nameInBytes = currencyData.getName().getBytes();
        byte[] symbolInBytes = currencyData.getSymbol().getBytes();
        byte[] descriptionInBytes = currencyData.getDescription() == null ? new byte[0] : currencyData.getDescription().getBytes();
        byte[] totalSupplyInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes();
        int scale = currencyData.getScale();
        long creationTime = currencyData.getCreationTime().toEpochMilli();
        byte[] typeInBytes = currencyData.getType().toString().getBytes();
        byte[] originatorHashInBytes = currencyData.getOriginatorHash().getBytes();
        byte[] registrarHashInBytes = currencyData.getRegistrarHash().getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(hashInBytes.length + nameInBytes.length + symbolInBytes.length +
                descriptionInBytes.length + totalSupplyInBytes.length + Integer.BYTES + Long.BYTES +
                typeInBytes.length + originatorHashInBytes.length + registrarHashInBytes.length);
        buffer.put(hashInBytes).put(nameInBytes).put(symbolInBytes).put(descriptionInBytes).put(totalSupplyInBytes).
                putInt(scale).putLong(creationTime).put(typeInBytes).put(originatorHashInBytes).put(registrarHashInBytes);

        return CryptoHelper.cryptoHash(buffer.array()).getBytes();
    }
}
