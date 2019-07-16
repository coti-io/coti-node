package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.utils.HashTestUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.ByteBuffer;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={AddressesRequestCrypto.class, CryptoHelper.class})
public class AddressesRequestCryptoTest {

    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;

    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;

    @Test
    public void testGetSignatureEqual(){
        List<Hash> addressHashes = HashTestUtils.generateListOfRandomHashes(10);
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashes));
        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        GetAddressesBulkRequest getAddressesBulkRequest = new GetAddressesBulkRequest(addressHashes);
        Assert.assertArrayEquals( bytes, addressesRequestCrypto.getSignatureMessage(getAddressesBulkRequest) );
    }

    @Test
    public void testGetSignatureNotEqual(){
        List<Hash> addressHashesOne = HashTestUtils.generateListOfRandomHashes(10);
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashesOne));
        addressHashesOne.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();

        List<Hash> addressHashesTwo = HashTestUtils.generateListOfRandomHashes(10);
        GetAddressesBulkRequest getAddressesBulkRequest = new GetAddressesBulkRequest(addressHashesTwo);
        Assert.assertThat(bytes, IsNot.not(IsEqual.equalTo(addressesRequestCrypto.getSignatureMessage(getAddressesBulkRequest))));
    }

    private int getByteBufferSize(List<Hash> addressHashes){
        int size = 0;
        for( Hash hash : addressHashes){
            size += hash.getBytes().length;
        }
        return size;
    }
}
