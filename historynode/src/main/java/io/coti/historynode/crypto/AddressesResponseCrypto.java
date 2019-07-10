package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesBulkResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;

@Service
public class AddressesResponseCrypto extends SignatureValidationCrypto<GetAddressesBulkResponse> {
    @Override
    public byte[] getSignatureMessage(GetAddressesBulkResponse getAddressesBulkResponse) {
        Map<Hash, AddressData> addressHashesToAddresses = getAddressesBulkResponse.getAddressHashesToAddresses();
        Hash firstKey = addressHashesToAddresses.entrySet().iterator().next().getKey();
        int byteBufferSize = getByteBufferSize(addressHashesToAddresses.get(firstKey), addressHashesToAddresses.size());
        ByteBuffer addressesResponseBuffer = ByteBuffer.allocate(byteBufferSize);
        //TODO 7/2/2019 astolia: don't sign both hashes. just make sure they are the same.
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