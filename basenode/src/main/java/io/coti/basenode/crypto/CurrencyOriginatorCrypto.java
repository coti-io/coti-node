package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;

import java.nio.ByteBuffer;

public class CurrencyOriginatorCrypto extends SignatureValidationCrypto<CurrencyData> {

    @Override
    public byte[] getSignatureMessage(CurrencyData currencyData) {
        byte[] nameInBytes = currencyData.getName().getBytes();
        byte[] symbolInBytes = currencyData.getSymbol().getBytes();
        byte[] descriptionInBytes = currencyData.getDescription().getBytes();
        byte[] totalSupplyInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes();
        byte[] scaleInBytes = ByteBuffer.allocate(Integer.BYTES).putInt(currencyData.getScale()).array();
        ByteBuffer currencyBuffer = ByteBuffer.allocate(nameInBytes.length + symbolInBytes.length + descriptionInBytes.length + totalSupplyInBytes.length + totalSupplyInBytes.length + scaleInBytes.length)
                .put(nameInBytes).put(symbolInBytes).put(descriptionInBytes).put(totalSupplyInBytes).put(totalSupplyInBytes).put(scaleInBytes);
        return CryptoHelper.cryptoHash(currencyBuffer.array()).getBytes();
    }

    @Override
    public SignatureData getSignature(CurrencyData currencyData) {
        return currencyData.getOriginatorSignature();
    }

    @Override
    public Hash getSignerHash(CurrencyData currencyData) {
        return currencyData.getOriginatorHash();
    }
}
