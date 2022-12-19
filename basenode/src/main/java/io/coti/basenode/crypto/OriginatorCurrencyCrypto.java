package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.SignatureData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class OriginatorCurrencyCrypto extends SignatureCrypto<OriginatorCurrencyData> {

    @Override
    public byte[] getSignatureMessage(OriginatorCurrencyData currencyData) {
        byte[] nameInBytes = currencyData.getName().getBytes(StandardCharsets.UTF_8);
        byte[] symbolInBytes = currencyData.getSymbol().getBytes(StandardCharsets.UTF_8);
        byte[] descriptionInBytes = currencyData.getDescription().getBytes(StandardCharsets.UTF_8);
        byte[] totalSupplyInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] scaleInBytes = ByteBuffer.allocate(Integer.BYTES).putInt(currencyData.getScale()).array();
        ByteBuffer currencyBuffer = ByteBuffer.allocate(nameInBytes.length + symbolInBytes.length + descriptionInBytes.length + totalSupplyInBytes.length + scaleInBytes.length)
                .put(nameInBytes).put(symbolInBytes).put(descriptionInBytes).put(totalSupplyInBytes).put(scaleInBytes);
        return CryptoHelper.cryptoHash(currencyBuffer.array()).getBytes();
    }

    public static byte[] getMessageInBytes(OriginatorCurrencyData currencyData) {
        SignatureData signatureData = currencyData.getSignature();
        return signatureData.getR().concat(signatureData.getS()).getBytes(StandardCharsets.UTF_8);
    }

    public static Hash calculateHash(String symbol) {
        return CryptoHelper.cryptoHash(symbol.toLowerCase().getBytes(), 224);
    }
}
