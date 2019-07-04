package io.coti.historynode.crypto;

import io.coti.basenode.http.GetAddressesRequest;
import io.coti.basenode.http.GetAddressesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryAddressCrypto {

    @Autowired
    private AddressesResponseCrypto addressesResponseCrypto;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;

    public boolean getAddressResponseSignatureMessage(GetAddressesResponse getAddressesResponse){
        return addressesResponseCrypto.verifySignature(getAddressesResponse);
    }
    public boolean verifyGetAddressRequestSignatureMessage(GetAddressesRequest getAddressesRequest){
        return addressesRequestCrypto.verifySignature(getAddressesRequest);
    }
}