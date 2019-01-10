package io.coti.basenode.crypto;

import io.coti.basenode.http.RollingReserveMerchantAddressRequest;
import org.springframework.stereotype.Service;



@Service
public class RollingReserveMerchantAddressCrypto extends SignatureCrypto<RollingReserveMerchantAddressRequest> {
        @Override
        public byte[] getSignatureMessage(RollingReserveMerchantAddressRequest signable) {
            return CryptoHelper.cryptoHash(signable.getMerchantHash().getBytes()).getBytes();
        }
}
