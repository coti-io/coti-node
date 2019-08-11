package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
@Slf4j
public class GetHistoryAddressesRequestCrypto extends SignatureCrypto<GetHistoryAddressesRequest> {

    private final int SIZE_OF_ADDRESS_HASH_IN_BYTES = 68;

    @Override
    public byte[] getSignatureMessage(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        List<Hash> addressHashes = getHistoryAddressesRequest.getAddressHashes();
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(SIZE_OF_ADDRESS_HASH_IN_BYTES * addressHashes.size());
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        return CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();
    }

}