package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.MerchantRollingReserveData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class MerchantRollingReserveCrypto extends SignatureCrypto<MerchantRollingReserveData> {

    @Override
    public byte[] getSignatureMessage(MerchantRollingReserveData merchantRollingReserveData) {

        int byteBufferLength;
        byte[] merchantHashInBytes;
        byte[] rollingReserveAddressInBytes = null;

        merchantHashInBytes = merchantRollingReserveData.getMerchantHash().getBytes();
        byteBufferLength = merchantHashInBytes.length;

        if (merchantRollingReserveData.getRollingReserveAddress() != null) {
            rollingReserveAddressInBytes = merchantRollingReserveData.getRollingReserveAddress().getBytes();
            byteBufferLength += rollingReserveAddressInBytes.length;
        }

        ByteBuffer rollingReserveAddressDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(merchantHashInBytes);

        if (rollingReserveAddressInBytes != null) {
            rollingReserveAddressDataBuffer.put(rollingReserveAddressInBytes);
        }

        byte[] rollingReserveAddressDataInBytes = rollingReserveAddressDataBuffer.array();
        return CryptoHelper.cryptoHash(rollingReserveAddressDataInBytes).getBytes();
    }
}

