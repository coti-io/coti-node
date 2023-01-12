package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.RequestedAddressHashData;
import io.coti.basenode.http.AddressBulkRequest;
import io.coti.basenode.http.AddressFileRequest;
import io.coti.basenode.http.AddressesExistsResponse;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

public interface IAddressService {
    void init();

    Boolean addNewAddress(AddressData addressData);

    boolean addressExists(Hash addressHash);

    void handlePropagatedAddress(AddressData addressData);

    boolean validateAddress(Hash addressHash);

    void getAddressBatch(HttpServletResponse response);

    ResponseEntity<IResponse> uploadAddressBatch(AddressFileRequest request);

    boolean validateRequestedAddressHashExistsAndRelevant(RequestedAddressHashData requestedAddressHashData);

    void handleNewAddressFromFullNode(AddressData data);

    Boolean addAddress(Hash address);

    ResponseEntity<IResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest);

    AddressesExistsResponse addressesCheckExistenceAndRequestHistoryNode(AddressBulkRequest addressRequest);
}
