package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyTypeCrypto extends SignatureCrypto<CurrencyTypeData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeData currencyTypeData) {
        byte[] currencySymbolInBytes = currencyTypeData.getSymbol().getBytes();
        byte[] currencyTypeInBytes = currencyTypeData.getCurrencyType().name().getBytes();
        byte[] bytesOfCurrencyRateSourceType = currencyTypeData.getCurrencyRateSourceType() == null ? new byte[0] : currencyTypeData.getCurrencyRateSourceType().name().getBytes();
        byte[] bytesOfRateSource = currencyTypeData.getRateSource() == null ? new byte[0] : currencyTypeData.getRateSource().getBytes();
        byte[] bytesOfProtectionModel = currencyTypeData.getProtectionModel() == null ? new byte[0] : currencyTypeData.getProtectionModel().getBytes();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyTypeData.getCreateTime().toEpochMilli()).array();

        ByteBuffer currencyTypeRegistrationBuffer = ByteBuffer.allocate(currencySymbolInBytes.length + currencyTypeInBytes.length
                + bytesOfCurrencyRateSourceType.length + bytesOfRateSource.length + bytesOfProtectionModel.length + creationTimeInBytes.length)
                .put(currencySymbolInBytes).put(currencyTypeInBytes).put(bytesOfCurrencyRateSourceType).put(bytesOfRateSource).put(bytesOfProtectionModel).put(creationTimeInBytes);
        return CryptoHelper.cryptoHash(currencyTypeRegistrationBuffer.array()).getBytes();
    }

}
