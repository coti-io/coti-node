package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.MintingRequestData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class MintingRequestCrypto extends SignatureCrypto<MintingRequestData> {

    @Override
    public byte[] getSignatureMessage(MintingRequestData mintingRequestData) {
        byte[] hashInBytes = mintingRequestData.getHash().toString().getBytes();
        byte[] warrantHashInBytes = mintingRequestData.getMintingFeeWarrantHash().toString().getBytes();
        byte[] mintingRequestTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(mintingRequestData.getMintingRequestTime().toEpochMilli()).array();
        byte[] amountInBytes = mintingRequestData.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] receiverAddressInBytes = mintingRequestData.getReceiverAddress().toString().getBytes();

        ByteBuffer mintingRequestBuffer = ByteBuffer.allocate(hashInBytes.length + warrantHashInBytes.length + mintingRequestTimeInBytes.length +
                amountInBytes.length + receiverAddressInBytes.length)
                .put(hashInBytes).put(warrantHashInBytes).put(mintingRequestTimeInBytes).put(amountInBytes).put(receiverAddressInBytes);

        return CryptoHelper.cryptoHash(mintingRequestBuffer.array()).getBytes();
    }
}
