package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.historynode.http.GetAddressesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;

@Service
public class AddressesResponseCrypto extends SignatureValidationCrypto<GetAddressesResponse> {
    @Override
    public byte[] getSignatureMessage(GetAddressesResponse getAddressesResponse) {
        Map<Hash, AddressData> addressHashesToAddresses = getAddressesResponse.getAddressHashesToAddresses();
        Hash firstKey = addressHashesToAddresses.entrySet().iterator().next().getKey();
        int byteBufferSize = getByteBufferSize(addressHashesToAddresses.get(firstKey), addressHashesToAddresses.size());
        ByteBuffer addressesResponseBuffer = ByteBuffer.allocate(byteBufferSize);
        addressHashesToAddresses.forEach((hash,addressHash) ->
                addressesResponseBuffer.
                        put(hash.getBytes()).
                        put(addressHash.getHash().getBytes()).
                        putLong(addressHash.getCreationTime().toEpochMilli()));

        byte[] addressesResponseInBytes = addressesResponseBuffer.array();
        return CryptoHelper.cryptoHash(addressesResponseInBytes).getBytes();
    }


    private int getByteBufferSize(AddressData addressData, int entries){
        int size = 0;
        size += (addressData.getHash().getBytes().length * 2);
        size += Long.BYTES;
        return (size * entries);
    }
}