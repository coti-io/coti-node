package io.coti.historynode.crypto;

import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.GetAddressesBulkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryAddressCrypto {

    @Autowired
    private AddressesResponseCrypto addressesResponseCrypto;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;

    public boolean getAddressResponseSignatureMessage(GetAddressesBulkResponse getAddressesBulkResponse){
        return addressesResponseCrypto.verifySignature(getAddressesBulkResponse);
    }
    public boolean verifyGetAddressRequestSignatureMessage(GetAddressesBulkRequest getAddressesBulkRequest){
        return addressesRequestCrypto.verifySignature(getAddressesBulkRequest);
    }
}