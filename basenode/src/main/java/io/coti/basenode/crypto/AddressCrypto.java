package io.coti.basenode.crypto;

import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.GetAddressesBulkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressCrypto {

    @Autowired
    private AddressesResponseCrypto addressesResponseCrypto;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;

    public boolean verifyGetAddressResponseSignatureMessage(GetAddressesBulkResponse getAddressesBulkResponse){
        return addressesResponseCrypto.verifySignature(getAddressesBulkResponse);
    }
    public boolean verifyGetAddressRequestSignatureMessage(GetAddressesBulkRequest getAddressesBulkRequest){
        return addressesRequestCrypto.verifySignature(getAddressesBulkRequest);
    }

    public void signAddressRequest(GetAddressesBulkRequest getAddressesBulkRequest){
        addressesRequestCrypto.signMessage(getAddressesBulkRequest);
    }

}