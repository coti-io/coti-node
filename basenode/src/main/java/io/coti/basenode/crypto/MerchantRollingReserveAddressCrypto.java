package io.coti.basenode.crypto;

import io.coti.basenode.http.MerchantRollingReserveAddressRequest;
import org.springframework.stereotype.Service;

@Service
public class MerchantRollingReserveAddressCrypto extends SignatureCrypto<MerchantRollingReserveAddressRequest> {
    @Override
    public byte[] getSignatureMessage(MerchantRollingReserveAddressRequest signable) {
        return CryptoHelper.cryptoHash(signable.getMerchantHash().getBytes()).getBytes();
    }
}
