package io.coti.basenode.crypto;

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
        int byteBufferSize = getByteBufferSize(addressHashesToAddresses);
        ByteBuffer addressesResponseBuffer = ByteBuffer.allocate(byteBufferSize);
        addressHashesToAddresses.forEach((hash,addressHash) -> {
            addressesResponseBuffer.
                    put(hash.getBytes());
            if(addressHash != null){
                addressesResponseBuffer.putLong(addressHash.getCreationTime().toEpochMilli());

            }
        });
        byte[] addressesResponseInBytes = addressesResponseBuffer.array();
        return CryptoHelper.cryptoHash(addressesResponseInBytes).getBytes();
    }

    private int getByteBufferSize(Map<Hash, AddressData> addressHashesToAddresses){
        int size = 0;
        for( Map.Entry<Hash, AddressData> entry : addressHashesToAddresses.entrySet()){
            size += entry.getKey().getBytes().length;
            if(entry.getValue() != null){
                size += Long.BYTES;
            }
        }
        return size;
    }
}