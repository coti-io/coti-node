package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.MintingTokenFeeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class MintingTokenFeeRequestCrypto extends SignatureValidationCrypto<MintingTokenFeeRequest> {

    @Override
    public byte[] getSignatureMessage(MintingTokenFeeRequest mintingTokenFeeRequest) {
        byte[] currencyHashInBytes = mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash().getBytes();
        byte[] amountBytes = mintingTokenFeeRequest.getMintingFeeData().getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] addressHashInBytes = mintingTokenFeeRequest.getReceiverAddress().getBytes();

        byte[] mintingTokenFeeRequestBufferInBytes = ByteBuffer.allocate(currencyHashInBytes.length + amountBytes.length + addressHashInBytes.length + Long.BYTES).
                put(currencyHashInBytes).put(amountBytes).put(addressHashInBytes).putLong(mintingTokenFeeRequest.getMintingFeeData().getCreationTime().toEpochMilli()).array();

        return CryptoHelper.cryptoHash(mintingTokenFeeRequestBufferInBytes).getBytes();
    }
}
