package io.coti.basenode.crypto;

import io.coti.basenode.http.GetMerchantRollingReserveAddressRequest;
import org.springframework.stereotype.Component;

@Component
public class GetMerchantRollingReserveAddressCrypto extends SignatureCrypto<GetMerchantRollingReserveAddressRequest> {
    @Override
    public byte[] getSignatureMessage(GetMerchantRollingReserveAddressRequest signable) {
        return signable.getMerchantHash().getBytes();
    }
}
