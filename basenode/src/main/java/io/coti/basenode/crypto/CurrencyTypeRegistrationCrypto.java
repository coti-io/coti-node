package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeRegistrationData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyTypeRegistrationCrypto extends SignatureCrypto<CurrencyTypeRegistrationData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeRegistrationData currencyTypeRegistrationData) {
        byte[] symbolInBytes = currencyTypeRegistrationData.getSymbol().getBytes();
        byte[] currencyTypeInBytes = currencyTypeRegistrationData.getCurrencyType().name().getBytes();
        byte[] bytesOfCurrencyRateSourceType = currencyTypeRegistrationData.getCurrencyRateSourceType() == null ? new byte[0] : currencyTypeRegistrationData.getCurrencyRateSourceType().name().getBytes();
        byte[] bytesOfRateSource = currencyTypeRegistrationData.getRateSource() == null ? new byte[0] : currencyTypeRegistrationData.getRateSource().getBytes();
        byte[] bytesOfProtectionModel = currencyTypeRegistrationData.getProtectionModel() == null ? new byte[0] : currencyTypeRegistrationData.getProtectionModel().getBytes();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyTypeRegistrationData.getCreateTime().toEpochMilli()).array();

        ByteBuffer currencyTypeRegistrationBuffer = ByteBuffer.allocate(symbolInBytes.length + currencyTypeInBytes.length
                + bytesOfCurrencyRateSourceType.length + bytesOfRateSource.length + bytesOfProtectionModel.length + creationTimeInBytes.length)
                .put(symbolInBytes).put(currencyTypeInBytes).put(bytesOfCurrencyRateSourceType).put(bytesOfRateSource).put(bytesOfProtectionModel).put(creationTimeInBytes);
        return CryptoHelper.cryptoHash(currencyTypeRegistrationBuffer.array()).getBytes();
    }

}
