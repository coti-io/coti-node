package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.RollingReserveAddressData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class RollingReserveCrypto extends SignatureCrypto<RollingReserveAddressData> {

    @Override
    public byte[] getMessageInBytes(RollingReserveAddressData rollingReserveAddressData) {

        int byteBufferLength;
        byte[] merchantHashInBytes;
        byte[] rollingReserveAddressInBytes = null;

        merchantHashInBytes = rollingReserveAddressData.getMerchantHash().getBytes();
        byteBufferLength = merchantHashInBytes.length;

        if(rollingReserveAddressData.getRollingReserveAddress() != null) {
            rollingReserveAddressInBytes = rollingReserveAddressData.getRollingReserveAddress().getBytes();
            byteBufferLength += rollingReserveAddressInBytes.length;
        }

        ByteBuffer rollingReserveAddressDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(merchantHashInBytes);

        if(rollingReserveAddressInBytes != null) {
            rollingReserveAddressDataBuffer.put(rollingReserveAddressInBytes);
        }

        byte[] rollingReserveAddressDataInBytes = rollingReserveAddressDataBuffer.array();
        return CryptoHelper.cryptoHash(rollingReserveAddressDataInBytes).getBytes();
    }
}

