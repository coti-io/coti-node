package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.SignatureData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class OriginatorCurrencyCrypto extends SignatureCrypto<OriginatorCurrencyData> {

    @Override
    public byte[] getSignatureMessage(OriginatorCurrencyData currencyData) {
        byte[] nameInBytes = currencyData.getName().getBytes();
        byte[] symbolInBytes = currencyData.getSymbol().getBytes();
        byte[] descriptionInBytes = currencyData.getDescription().getBytes();
        byte[] totalSupplyInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes();
        byte[] scaleInBytes = ByteBuffer.allocate(Integer.BYTES).putInt(currencyData.getScale()).array();
        ByteBuffer currencyBuffer = ByteBuffer.allocate(nameInBytes.length + symbolInBytes.length + descriptionInBytes.length + totalSupplyInBytes.length + scaleInBytes.length)
                .put(nameInBytes).put(symbolInBytes).put(descriptionInBytes).put(totalSupplyInBytes).put(scaleInBytes);
        return CryptoHelper.cryptoHash(currencyBuffer.array()).getBytes();
    }

}
