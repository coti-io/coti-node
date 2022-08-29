package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.utils.HashTestUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GetHistoryAddressesRequestCrypto.class, CryptoHelper.class})
public class AddressesRequestCryptoTest {

    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;

    @Test
    public void testGetSignatureEqual() {
        List<Hash> addressHashes = HashTestUtils.generateListOfRandomAddressHashes(10);
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashes);
        Instant createTime = getHistoryAddressesRequest.getCreateTime();

        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashes) + Long.BYTES);
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        addressesRequestBuffer.putLong(createTime.toEpochMilli());
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        Assert.assertArrayEquals(bytes, getHistoryAddressesRequestCrypto.getSignatureMessage(getHistoryAddressesRequest));
    }

    @Test
    public void testGetSignatureNotEqual() {
        List<Hash> addressHashesTwo = HashTestUtils.generateListOfRandomHashes(10);
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashesTwo);
        Instant createTime = getHistoryAddressesRequest.getCreateTime();

        List<Hash> addressHashesOne = HashTestUtils.generateListOfRandomAddressHashes(10);
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashesOne) + Long.BYTES);
        addressHashesOne.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        addressesRequestBuffer.putLong(createTime.toEpochMilli());
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        Assert.assertThat(bytes, IsNot.not(IsEqual.equalTo(getHistoryAddressesRequestCrypto.getSignatureMessage(getHistoryAddressesRequest))));
    }

    private int getByteBufferSize(List<Hash> addressHashes) {
        int size = 0;
        for (Hash hash : addressHashes) {
            size += hash.getBytes().length;
        }
        return size;
    }
}
