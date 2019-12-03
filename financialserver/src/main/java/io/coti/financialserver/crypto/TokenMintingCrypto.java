package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.TokenMintingData;
import org.springframework.stereotype.Service;

@Service
public class TokenMintingCrypto extends SignatureValidationCrypto<TokenMintingData> {

    @Override
    public byte[] getSignatureMessage(TokenMintingData tokenMintingData) {
        return CryptoHelper.cryptoHash(tokenMintingData.getMessageInBytes()).getBytes();
    }
}
