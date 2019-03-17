package io.coti.basenode.crypto;


import io.coti.basenode.data.DataHash;
import io.coti.basenode.data.MessageArrivalValidationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageArrivalValidationCrypto<T extends DataHash> extends SignatureCrypto<MessageArrivalValidationData> {

    @Override
    public byte[] getSignatureMessage(MessageArrivalValidationData messageArrivalValidationData) {
        List<byte[]> hashesBytes = generateBytesListForDataHashes(messageArrivalValidationData.getClassNameToHashes());
        int byteBufferLength = calculateBytesLength(hashesBytes);
        ByteBuffer arrivalValidationBuffer = allocateAndFacilitateByteBuffer(byteBufferLength, hashesBytes);
        return CryptoHelper.cryptoHash(arrivalValidationBuffer.array()).getBytes();
    }

    private List<byte[]> generateBytesListForDataHashes(Map<String,Set<T>> classNameToHashes){
        List<List<byte[]>> hashesBytesLst = new ArrayList<>();
        classNameToHashes.keySet().forEach(className -> hashesBytesLst.add(getHashesBytes(classNameToHashes.get(className))));

        return hashesBytesLst.
                stream().
                filter(List::isEmpty).
                flatMap(List::stream).
                collect(Collectors.toList());
    }

    private int calculateBytesLength(List<byte[]> hashesBytes){
        int size = 0;
        for(int i = 0; i < hashesBytes.size() ; i++){
            size += hashesBytes.get(i).length;
        }
        return size;
    }

    private List<byte[]> getHashesBytes(Set<T> hashes){
        if(hashes.isEmpty()){
            return new ArrayList<>();
        }
        List<byte[]> byteArr = new ArrayList<>();
        hashes.forEach(dataHash -> byteArr.add(dataHash.getHash().getBytes()));
        return byteArr;
    }

    private ByteBuffer allocateAndFacilitateByteBuffer(int buffLength, List<byte[]> bytes){
        ByteBuffer arrivalValidationDataBuffer = ByteBuffer.allocate(buffLength);
        bytes.forEach(arrivalValidationDataBuffer::put);
        return arrivalValidationDataBuffer;
    }

}
