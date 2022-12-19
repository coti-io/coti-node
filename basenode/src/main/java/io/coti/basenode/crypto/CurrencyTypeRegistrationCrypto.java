package io.coti.basenode.crypto;

import io.coti.basenode.data.CurrencyTypeRegistrationData;
import io.coti.basenode.data.SignatureData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class CurrencyTypeRegistrationCrypto extends SignatureCrypto<CurrencyTypeRegistrationData> {

    @Override
    public byte[] getSignatureMessage(CurrencyTypeRegistrationData currencyTypeRegistrationData) {
        byte[] symbolInBytes = currencyTypeRegistrationData.getSymbol().getBytes();
        byte[] currencyTypeInBytes = currencyTypeRegistrationData.getCurrencyType().name().getBytes();
        byte[] bytesOfCurrencyRateSourceType = currencyTypeRegistrationData.getCurrencyRateSourceType() == null ? new byte[0] : currencyTypeRegistrationData.getCurrencyRateSourceType().name().getBytes();
        byte[] bytesOfRateSource = currencyTypeRegistrationData.getRateSource() == null ? new byte[0] : currencyTypeRegistrationData.getRateSource().getBytes();
        byte[] bytesOfProtectionModel = currencyTypeRegistrationData.getProtectionModel() == null ? new byte[0] : currencyTypeRegistrationData.getProtectionModel().getBytes();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(currencyTypeRegistrationData.getCreateTime().toEpochMilli()).array();

        ByteBuffer currencyTypeRegistrationBuffer = ByteBuffer.allocate(symbolInBytes.length + currencyTypeInBytes.length
                        + bytesOfCurrencyRateSourceType.length + bytesOfRateSource.length + bytesOfProtectionModel.length + createTimeInBytes.length)
                .put(symbolInBytes).put(currencyTypeInBytes).put(bytesOfCurrencyRateSourceType).put(bytesOfRateSource).put(bytesOfProtectionModel).put(createTimeInBytes);
        return CryptoHelper.cryptoHash(currencyTypeRegistrationBuffer.array()).getBytes();
    }

    public static byte[] getMessageInBytes(CurrencyTypeRegistrationData currencyTypeRegistrationData) {
        SignatureData signatureData = currencyTypeRegistrationData.getSignature();
        return signatureData.getR().concat(signatureData.getS()).getBytes(StandardCharsets.UTF_8);
    }
}
