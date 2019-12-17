package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

import static io.coti.basenode.crypto.CryptoHelper.ADDRESS_SIZE_IN_BYTES;

@Service
@Slf4j
public class GetHistoryAddressesRequestCrypto extends SignatureCrypto<GetHistoryAddressesRequest> {

    @Override
    public byte[] getSignatureMessage(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        List<Hash> addressHashes = getHistoryAddressesRequest.getAddressHashes();
        Instant createTime = getHistoryAddressesRequest.getCreateTime();
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(ADDRESS_SIZE_IN_BYTES * addressHashes.size() + Long.BYTES);
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        addressesRequestBuffer.putLong(createTime.toEpochMilli());
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        return CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();
    }

}