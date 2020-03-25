package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CurrencyTypeCrypto extends SignatureCrypto<CurrencyTypeData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeData currencyTypeData) {
        byte[] currencySymbolInBytes = currencyTypeData.getSymbol().getBytes();
        byte[] currencyTypeInBytes = currencyTypeData.getCurrencyType().getText().getBytes();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyTypeData.getCreateTime().toEpochMilli()).array();
        byte[] bytesOfCurrencyRateSourceType = currencyTypeData.getCurrencyRateSourceType() == null ? new byte[0] : currencyTypeData.getCurrencyRateSourceType().getText().getBytes();
        byte[] bytesOfRateSource = currencyTypeData.getRateSource() == null ? new byte[0] : currencyTypeData.getRateSource().getBytes();
        byte[] bytesOfProtectionModel = currencyTypeData.getProtectionModel() == null ? new byte[0] : currencyTypeData.getProtectionModel().getBytes();


        ByteBuffer currencyTypeRegistrationBuffer = ByteBuffer.allocate(currencySymbolInBytes.length + currencyTypeInBytes.length
                + creationTimeInBytes.length + bytesOfCurrencyRateSourceType.length + bytesOfRateSource.length + bytesOfProtectionModel.length)
                .put(currencySymbolInBytes).put(currencyTypeInBytes).put(bytesOfCurrencyRateSourceType).put(bytesOfRateSource).put(bytesOfProtectionModel);
        return CryptoHelper.cryptoHash(currencyTypeRegistrationBuffer.array()).getBytes();
    }

}
