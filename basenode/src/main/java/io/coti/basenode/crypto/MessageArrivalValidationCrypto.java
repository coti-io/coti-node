package io.coti.basenode.crypto;


import io.coti.basenode.data.MessageArrivalValidationData;
import org.springframework.stereotype.Service;

@Service
public class MessageArrivalValidationCrypto extends SignatureCrypto<MessageArrivalValidationData> {

    @Override
    public byte[] getSignatureMessage(MessageArrivalValidationData signable) {
        return signable.getHash().getBytes();
    }

    /*
    byte[] nodeTypeInBytes = getNodeRegistrationRequest.getNodeType().getBytes();
        byte[] networkTypeInBytes = getNodeRegistrationRequest.getNetworkType().getBytes();

        ByteBuffer nodeRegistrationBuffer = ByteBuffer.allocate(nodeTypeInBytes.length + networkTypeInBytes.length).put(nodeTypeInBytes).put(networkTypeInBytes);
        return CryptoHelper.cryptoHash(nodeRegistrationBuffer.array()).getBytes();
     */

    /*
    byte[] userHashInBytes = transactionTrustScoreData.getUserHash().getBytes();

        byte[] transactionHashInBytes = transactionTrustScoreData.getTransactionHash().getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(transactionTrustScoreData.getTrustScore());

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + transactionHashInBytes.length + 8).
                put(userHashInBytes).put(transactionHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
     */

}
