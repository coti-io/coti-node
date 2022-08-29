package io.coti.basenode.crypto;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.utils.AddressTestUtils;
import io.coti.basenode.utils.HashTestUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GetHistoryAddressesResponseCrypto.class, CryptoHelper.class})
public class AddressesResponseCryptoTest {

    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;

    @Test
    public void testGetSignature() {
        int size = 10;
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        Map<Hash, AddressData> addressHashesToAddresses = new LinkedHashMap<>();
        addresses.forEach(addressData -> addressHashesToAddresses.put(addressData.getHash(), addressData));
        Hash hash = HashTestUtils.generateRandomAddressHash();
        addressHashesToAddresses.put(hash, null);

        GetHistoryAddressesResponse getHistoryAddressesResponse = new GetHistoryAddressesResponse(addressHashesToAddresses);
        Instant createTime = getHistoryAddressesResponse.getCreateTime();

        ByteBuffer addressesResponseBuffer = ByteBuffer.allocate(getByteBufferSize(addressHashesToAddresses) + Long.BYTES);
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddresses.entrySet()) {
            addressesResponseBuffer.put(entry.getKey().getBytes());
            if (entry.getValue() != null) {
                addressesResponseBuffer.putLong(entry.getValue().getCreationTime().toEpochMilli());
            }
        }
        addressesResponseBuffer.putLong(createTime.toEpochMilli());

        byte[] addressesResponseInBytes = addressesResponseBuffer.array();
        byte[] bytes = CryptoHelper.cryptoHash(addressesResponseInBytes).getBytes();
        Assert.assertArrayEquals(bytes, getHistoryAddressesResponseCrypto.getSignatureMessage(getHistoryAddressesResponse));
    }

    @Test
    public void testGetSignatureOrdered() {
        int size = 10;
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);

        Map<Hash, AddressData> addressHashesToAddressesOne = new LinkedHashMap<>();
        addresses.forEach(addressData -> addressHashesToAddressesOne.put(addressData.getHash(), addressData));
        Hash hash = HashTestUtils.generateRandomAddressHash();
        addressHashesToAddressesOne.put(hash, null);

        GetHistoryAddressesResponse getHistoryAddressesResponseOne = new GetHistoryAddressesResponse(addressHashesToAddressesOne);
        Instant createTime = getHistoryAddressesResponseOne.getCreateTime();

        ByteBuffer addressesResponseBufferOne = ByteBuffer.allocate(getByteBufferSize(addressHashesToAddressesOne) + Long.BYTES);
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddressesOne.entrySet()) {
            addressesResponseBufferOne.put(entry.getKey().getBytes());
            if (entry.getValue() != null) {
                addressesResponseBufferOne.putLong(entry.getValue().getCreationTime().toEpochMilli());
            }
        }
        addressesResponseBufferOne.putLong(createTime.toEpochMilli());

        byte[] addressesResponseInBytesOne = addressesResponseBufferOne.array();
        byte[] bytesOne = CryptoHelper.cryptoHash(addressesResponseInBytesOne).getBytes();
        Assert.assertArrayEquals(bytesOne, getHistoryAddressesResponseCrypto.getSignatureMessage(getHistoryAddressesResponseOne));

        //------------------------------

        Map<Hash, AddressData> addressHashesToAddressesTwo = new LinkedHashMap<>();
        addressHashesToAddressesTwo.put(hash, null);
        addresses.forEach(addressData -> addressHashesToAddressesTwo.put(addressData.getHash(), addressData));

        ByteBuffer addressesResponseBufferTwo = ByteBuffer.allocate(getByteBufferSize(addressHashesToAddressesTwo) + Long.BYTES);
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddressesTwo.entrySet()) {
            addressesResponseBufferTwo.put(entry.getKey().getBytes());
            if (entry.getValue() != null) {
                addressesResponseBufferTwo.putLong(entry.getValue().getCreationTime().toEpochMilli());
            }
        }
        GetHistoryAddressesResponse getHistoryAddressesResponseTwo = new GetHistoryAddressesResponse(addressHashesToAddressesTwo);
        Instant createTimeTwo = getHistoryAddressesResponseTwo.getCreateTime();
        addressesResponseBufferTwo.putLong(createTimeTwo.toEpochMilli());

        byte[] addressesResponseInBytesTwo = addressesResponseBufferTwo.array();
        byte[] bytesTwo = CryptoHelper.cryptoHash(addressesResponseInBytesTwo).getBytes();
        Assert.assertArrayEquals(bytesTwo, getHistoryAddressesResponseCrypto.getSignatureMessage(getHistoryAddressesResponseTwo));
        Assert.assertThat(bytesTwo, IsNot.not(IsEqual.equalTo(getHistoryAddressesResponseCrypto.getSignatureMessage(getHistoryAddressesResponseOne))));
    }

    private int getByteBufferSize(Map<Hash, AddressData> addressHashesToAddresses) {
        int size = 0;
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddresses.entrySet()) {
            size += entry.getKey().getBytes().length;
            if (entry.getValue() != null) {
                size += Long.BYTES;
            }
        }
        return size;
    }

}
