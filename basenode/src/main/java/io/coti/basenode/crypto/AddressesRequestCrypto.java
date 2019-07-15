package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesBulkRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class AddressesRequestCrypto extends SignatureCrypto<GetAddressesBulkRequest> {

    @Override
    public byte[] getSignatureMessage(GetAddressesBulkRequest getAddressesBulkRequest) {
        List<Hash> addressHashes = getAddressesBulkRequest.getAddressesHash();
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashes));
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        return CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();
    }

    private int getByteBufferSize(List<Hash> addressHashes){
        int size = 0;
        for( Hash hash : addressHashes){
            size += hash.getBytes().length;
        }
        return size;
    }
}