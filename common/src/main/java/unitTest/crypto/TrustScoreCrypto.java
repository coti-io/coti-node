package unitTest.crypto;

import io.coti.common.data.TrustScoreData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TrustScoreCrypto extends SignatureValidationCrypto<TrustScoreData> {

    @Override
    public byte[] getMessageInBytes(TrustScoreData trustScoreData) {
        byte[] userHashInBytes = trustScoreData.getUserHash().getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(trustScoreData.getKycTrustScore());

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + 8).
                put(userHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
