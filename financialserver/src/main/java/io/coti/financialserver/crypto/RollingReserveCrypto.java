package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.RollingReserveData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class RollingReserveCrypto extends SignatureCrypto<RollingReserveData> {

    @Override
    public byte[] getSignatureMessage(RollingReserveData rollingReserveData) {

        int byteBufferLength;
        byte[] merchantHashInBytes;
        byte[] rollingReserveAddressInBytes = null;

        merchantHashInBytes = rollingReserveData.getMerchantHash().getBytes();
        byteBufferLength = merchantHashInBytes.length;

        if (rollingReserveData.getRollingReserveAddress() != null) {
            rollingReserveAddressInBytes = rollingReserveData.getRollingReserveAddress().getBytes();
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

