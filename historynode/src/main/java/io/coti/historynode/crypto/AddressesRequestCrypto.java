package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesBulkRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Set;

@Service
public class AddressesRequestCrypto extends SignatureCrypto<GetAddressesBulkRequest> {

    @Override
    public byte[] getSignatureMessage(GetAddressesBulkRequest getAddressesBulkRequest) {
        Set<Hash> addressHashes = getAddressesBulkRequest.getAddressesHash();
        ByteBuffer addressesRequestBuffer = ByteBuffer.allocate(
                        (addressHashes.stream().findFirst().get().getBytes().length) * (addressHashes.size()));

        addressHashes.forEach(addressHash -> addressesRequestBuffer.put(addressHash.getBytes()));
        byte[] addressesRequestInBytes = addressesRequestBuffer.array();
        return CryptoHelper.cryptoHash(addressesRequestInBytes).getBytes();
    }

}
