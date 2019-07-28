package io.coti.historynode.services;

import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.historynode.http.HttpStringConstants.*;


@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {
    @Autowired
    private StorageConnector storageConnector;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;
    @Autowired
    private Addresses addresses;
    @Autowired
    private BaseNodeValidationService validationService;
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @Value("${storage.server.address}")
    private String storageServerAddress;

    public void handleClusterStampAddressesStorage() {
        List<AddressData> addressesData = new ArrayList<>();
        addresses.forEach(addressesData::add);
    }

    public ResponseEntity<IResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        try {
            if (!getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }

            List<Hash> addressesHashesToGetFromStorage = getHistoryAddressesRequest.getAddressHashes();
            Map<Hash, AddressData> addressToAddressDataFromDB = populateAndRemoveFoundAddresses(addressesHashesToGetFromStorage);
            ResponseEntity<GetHistoryAddressesResponse> storageResponse = getAddressesFromStorage(addressesHashesToGetFromStorage);

            GetHistoryAddressesResponse getHistoryAddressesResponse = storageResponse.getBody();

            if (!getHistoryAddressesResponseCrypto.verifySignature(getHistoryAddressesResponse)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(STORAGE_INVALID_SIGNATURE, STATUS_ERROR));
            }

            if (!validationService.validateGetAddressesResponse(getHistoryAddressesResponse)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(STORAGE_RESPONSE_VALIDATION_ERROR, STATUS_ERROR));
            }
            Map<Hash, AddressData> getHistoryAddressesResponseMap = reorderHashResponses(getHistoryAddressesRequest.getAddressHashes(), addressToAddressDataFromDB, storageResponse.getBody().getAddressHashesToAddresses());
            return ResponseEntity.
                    status(HttpStatus.OK).
                    body(new GetHistoryAddressesResponse(
                            getHistoryAddressesResponseMap));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(STORAGE_ADDRESS_ERROR, ((SeriazableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage()), STATUS_ERROR));
        }
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
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);
        return storageConnector.retrieveFromStorage(storageServerAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class);
    }
}
