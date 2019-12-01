package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.TokenMintingData;
import io.coti.financialserver.http.MintingTokenFeeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TokenMintingCrypto extends SignatureValidationCrypto<TokenMintingData> {

    @Override
    public byte[] getSignatureMessage(TokenMintingData tokenMintingData) {
        return CryptoHelper.cryptoHash(tokenMintingData.getMessageInBytes()).getBytes();
    }
}
