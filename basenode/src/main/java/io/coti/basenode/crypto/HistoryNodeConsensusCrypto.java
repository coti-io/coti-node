package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;
//import java.util.Date;

@Deprecated
@Service
public class HistoryNodeConsensusCrypto extends SignatureCrypto<HistoryNodeConsensusResult> {
    @Override
    public byte[] getSignatureMessage(HistoryNodeConsensusResult historyConsensusResult) {

        byte[] historyHashInBytes = historyConsensusResult.getHash().getBytes();

        Map<Hash, String> hashToObjectJsonDataMap = historyConsensusResult.getHashToObjectJsonDataMap(); // TODO check if data meant to be part of consensus or not
        byte[] hashToObjectJsonDataMapBytes = hashToObjectJsonDataMap.toString().getBytes();

//        ByteBuffer indexBuffer = ByteBuffer.allocate(8);
//        indexBuffer.putLong(historyConsensusResult.getIndex());
//
//        Date indexingTime = historyConsensusResult.getIndexingTime();
//        int timestamp = (int) (indexingTime.getTime());
//
//        ByteBuffer indexingTimeBuffer = ByteBuffer.allocate(4);
//        indexingTimeBuffer.putInt(timestamp);
//
//        ByteBuffer historyNodeConsensusMessageBuffer = ByteBuffer.allocate(historyHashInBytes.length + 8 + 4).
//                put(historyHashInBytes).put(indexBuffer.array()).put(indexingTimeBuffer.array());


        ByteBuffer historyNodeConsensusMessageBuffer = ByteBuffer.allocate(historyHashInBytes.length + hashToObjectJsonDataMapBytes.length).
                put(historyHashInBytes);

        byte[] historyConsensusMessageInBytes = historyNodeConsensusMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(historyConsensusMessageInBytes).getBytes();
        return cryptoHashedMessage;

    }
}
