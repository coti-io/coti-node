package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.Hash;
import io.coti.historynode.http.GetAddressesRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Set;

@Service
public class AddressesRequestCrypto extends SignatureValidationCrypto<GetAddressesRequest> {

    @Override
    public byte[] getSignatureMessage(GetAddressesRequest getAddressesRequest) {
        Set<Hash> addressHashes = getAddressesRequest.getAddressesHash();
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(
                        (addressHashes.stream().findFirst().get().getBytes().length) * (addressHashes.size()));

        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        return CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();
    }

}
