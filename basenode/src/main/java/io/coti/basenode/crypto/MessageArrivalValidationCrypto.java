package io.coti.basenode.crypto;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MessageArrivalValidationCrypto extends SignatureCrypto<MessageArrivalValidationData> {

    //TODO 3/18/2019 astolia: implement correct sign
    @Override
    public byte[] getSignatureMessage(MessageArrivalValidationData signable) {
        //TODO 3/18/2019 astolia: Figure out what is going on here and if is correct or not. also hoew to set hash.

        byte[] transactionHashesInBytes = getHashesBytes(signable.getTransactionHashes());
        byte[] addressHashesInBytes = getHashesBytes(signable.getAddressHashes());

        List<byte[]> hashesBytes = new ArrayList<>(Arrays.asList(transactionHashesInBytes, addressHashesInBytes));

        int byteBufferLength = transactionHashesInBytes.length + transactionHashesInBytes.length;

        ByteBuffer arrivalValidationBuffer = allocateAndFacilitateByteBuffer(byteBufferLength, hashesBytes);

        return CryptoHelper.cryptoHash(arrivalValidationBuffer.array()).getBytes();
    }

    private byte[] getHashesBytes(Set<?> hashes){
        if(hashes.isEmpty()){
            return new byte[]{};
        }
        //TODO 3/19/2019 astolia: Doesn't work well
        log.info(hashes.toString());
        return new Hash(hashes.toString()).getBytes();
    }

    private ByteBuffer allocateAndFacilitateByteBuffer(int buffLength, List<byte[]> bytes){
        ByteBuffer arrivalValidationDataBuffer = ByteBuffer.allocate(buffLength);
        bytes.forEach(byteArr -> arrivalValidationDataBuffer.put(byteArr));
        return arrivalValidationDataBuffer;
    }

}
