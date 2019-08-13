package io.coti.basenode.crypto;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;

import static io.coti.basenode.crypto.CryptoHelper.ADDRESS_SIZE_IN_BYTES;

@Service
public class GetHistoryAddressesResponseCrypto extends SignatureCrypto<GetHistoryAddressesResponse> {

    @Override
    public byte[] getSignatureMessage(GetHistoryAddressesResponse getHistoryAddressesResponse) {
        Map<Hash, AddressData> addressHashesToAddresses = getHistoryAddressesResponse.getAddressHashesToAddresses();
        int byteBufferSize = getByteBufferSize(addressHashesToAddresses);
        ByteBuffer addressesResponseBuffer = ByteBuffer.allocate(byteBufferSize);
        addressHashesToAddresses.forEach((hash, addressHash) -> {
            addressesResponseBuffer.
                    put(hash.getBytes());
            if (addressHash != null) {
                addressesResponseBuffer.putLong(addressHash.getCreationTime().toEpochMilli());

            }
        });
        byte[] addressesResponseInBytes = addressesResponseBuffer.array();
        return CryptoHelper.cryptoHash(addressesResponseInBytes).getBytes();
    }

    private int getByteBufferSize(Map<Hash, AddressData> addressHashesToAddresses) {
        int size = 0;
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddresses.entrySet()) {
            if (entry.getValue() != null) {
                size += Long.BYTES;
            }
        }
        size += (ADDRESS_SIZE_IN_BYTES * addressHashesToAddresses.size());
        return size;
    }
}