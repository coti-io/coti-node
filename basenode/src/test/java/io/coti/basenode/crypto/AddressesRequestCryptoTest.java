package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.utils.HashTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GetHistoryAddressesRequestCrypto.class, CryptoHelper.class})
class AddressesRequestCryptoTest {

    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @Test
    void testGetSignatureEqual() {
        List<Hash> addressHashes = HashTestUtils.generateListOfRandomAddressHashes(10);
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashes);
        Instant createTime = getHistoryAddressesRequest.getCreateTime();

        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashes) + Long.BYTES);
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        addressesRequestBuffer.putLong(createTime.toEpochMilli());
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        Assertions.assertArrayEquals(bytes, getHistoryAddressesRequestCrypto.getSignatureMessage(getHistoryAddressesRequest));
    }

    @Test
    void testGetSignatureNotEqual() {
        List<Hash> addressHashesTwo = HashTestUtils.generateListOfRandomHashes(10);
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashesTwo);
        Instant createTime = getHistoryAddressesRequest.getCreateTime();

        List<Hash> addressHashesOne = HashTestUtils.generateListOfRandomAddressHashes(10);
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashesOne) + Long.BYTES);
        addressHashesOne.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        addressesRequestBuffer.putLong(createTime.toEpochMilli());
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        Assertions.assertNotEquals(bytes, getHistoryAddressesRequestCrypto.getSignatureMessage(getHistoryAddressesRequest));
    }

    private int getByteBufferSize(List<Hash> addressHashes) {
        int size = 0;
        for (Hash hash : addressHashes) {
            size += hash.getBytes().length;
        }
        return size;
    }
}
