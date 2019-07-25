package io.coti.historynode.services;

import io.coti.basenode.crypto.AddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.basenode.services.BaseNodeValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;


@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {
    @Autowired
    protected StorageConnector storageConnector;
    @Autowired
    private Addresses addresses;
    @Autowired
    private BaseNodeValidationService validationService;
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;
    @Value("${storage.server.address}")
    private String storageServerAddress;

    public void handleClusterStampAddressesStorage() {
        List<AddressData> addressesData = new ArrayList<>();
        addresses.forEach(addressesData::add);
        // send all addresses to storage
        // Storage: store addresses
        // receive response from storage node.
        //delete addresses that were successfully stored.

        //TODO 7/15/2019 astolia: sign
        //TODO 7/15/2019 astolia: should be chunked.
//        getAddressesFromStorage(addressesData);
//        AddHistoryAddressesRequest addAddressesBulkRequest = new AddHistoryAddressesRequest(addressesData);
//        ResponseEntity<AddHistoryAddressesResponse> response = storageConnector.storeInStorage("TODO",addAddressesBulkRequest, AddHistoryAddressesResponse.class);
    }

    public ResponseEntity<IResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        if (!addressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        List<Hash> addressesHashesToGetFromStorage = getHistoryAddressesRequest.getAddressHashes();
        Map<Hash, AddressData> addressToAddressDataFromDB = populateAndRemoveFoundAddresses(addressesHashesToGetFromStorage);
        ResponseEntity<GetHistoryAddressesResponse> storageResponse = getAddressesFromStorage(addressesHashesToGetFromStorage);

        GetHistoryAddressesResponse getHistoryAddressesResponse = storageResponse.getBody();

        if (!getHistoryAddressesResponseCrypto.verifySignature(getHistoryAddressesResponse)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetHistoryAddressesResponse(new HashMap<>(), INVALID_SIGNATURE, STATUS_ERROR));
        }

        if (!validationService.validateGetAddressesResponse(getHistoryAddressesResponse)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetHistoryAddressesResponse(new HashMap<>(), "Response validation failed", STATUS_ERROR));
        }
        Map<Hash, AddressData> getHistoryAddressesResponseMap = reorderHashResponses(getHistoryAddressesRequest.getAddressHashes(), addressToAddressDataFromDB, storageResponse.getBody().getAddressHashesToAddresses());
        return ResponseEntity.
                status(HttpStatus.OK).
                body(new GetHistoryAddressesResponse(
                        getHistoryAddressesResponseMap));
    }

    private Map<Hash, AddressData> reorderHashResponses(List<Hash> originallyOrderedAddressHashes, Map<Hash, AddressData> addressHashesToAddressesFromDB, Map<Hash, AddressData> addressHashesToAddressesFromStorage) {
        Map<Hash, AddressData> orderedResponse = new LinkedHashMap<>();
        originallyOrderedAddressHashes.forEach(hash -> {
            AddressData addressData = Optional.ofNullable(addressHashesToAddressesFromDB.get(hash)).orElse(addressHashesToAddressesFromStorage.get(hash));
            orderedResponse.put(hash, addressData);
        });
        return orderedResponse;
    }

    private Map<Hash, AddressData> populateAndRemoveFoundAddresses(List<Hash> addressesHashes) {
        Map<Hash, AddressData> addressesFoundInDb = new HashMap<>();

        addressesHashes.removeIf(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if (addressData != null) {
                addressesFoundInDb.put(addressHash, addressData);
                return true;
            }
            return false;
        });

        return addressesFoundInDb;
    }

    private ResponseEntity<GetHistoryAddressesResponse> getAddressesFromStorage(List<Hash> addressesHashes) {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressesHashes);
        addressesRequestCrypto.signMessage(getHistoryAddressesRequest);
        return storageConnector.retrieveFromStorage(storageServerAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class);
    }
}
