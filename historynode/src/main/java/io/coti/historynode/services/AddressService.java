package io.coti.historynode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.RequestedAddressHashData;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.historynode.http.HttpStringConstants.*;
import static io.coti.historynode.services.NodeServiceManager.*;


@Slf4j
@Service
@Primary
public class AddressService extends BaseNodeAddressService {

    @Value("${storage.server.address}")
    private String storageServerAddress;

    @Override
    public ResponseEntity<IResponse> getAddresses(GetHistoryAddressesRequest getHistoryAddressesRequest) {
        try {
            if (!getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR));
            }

            List<Hash> addressesHashesToGetFromStorage = new ArrayList<>(getHistoryAddressesRequest.getAddressHashes());

            HashMap<Hash, AddressData> addressToAddressDataFromDB = populateAndRemoveFoundAddresses(addressesHashesToGetFromStorage);
            Map<Hash, AddressData> getHistoryAddressesResponseMap;

            if (!addressesHashesToGetFromStorage.isEmpty()) {
                GetHistoryAddressesResponse getHistoryAddressesResponseFromStorageNode = getAddressesFromStorage(addressesHashesToGetFromStorage);
                Optional<ResponseEntity<IResponse>> responseValidationResult = validateStorageResponse(getHistoryAddressesResponseFromStorageNode);
                if (responseValidationResult.isPresent()) {
//                    return responseValidationResult.get()
                    getHistoryAddressesResponseMap = addressToAddressDataFromDB;
                } else {
                    Map<Hash, AddressData> addressHashesToAddressesFromStorage = getHistoryAddressesResponseFromStorageNode.getAddressHashesToAddresses();
                    getHistoryAddressesResponseMap = reorderHashResponses(getHistoryAddressesRequest.getAddressHashes(), addressToAddressDataFromDB, addressHashesToAddressesFromStorage);
                }
            } else {
                getHistoryAddressesResponseMap = addressToAddressDataFromDB;
            }


            GetHistoryAddressesResponse getHistoryAddressesResponseToFullNode = new GetHistoryAddressesResponse(getHistoryAddressesResponseMap);
            getHistoryAddressesResponseCrypto.signMessage(getHistoryAddressesResponseToFullNode);
            return ResponseEntity.
                    status(HttpStatus.OK).
                    body(getHistoryAddressesResponseToFullNode);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(String.format(STORAGE_ADDRESS_ERROR, ((SerializableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage()), STATUS_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(e.getMessage(), STATUS_ERROR));
        }
    }

    private Map<Hash, AddressData> reorderHashResponses(List<Hash> originallyOrderedAddressHashes, Map<Hash, AddressData> addressHashesToAddressesFromDB, Map<Hash, AddressData> addressHashesToAddressesFromStorage) {
        Map<Hash, AddressData> orderedResponse = new LinkedHashMap<>();
        originallyOrderedAddressHashes.forEach(hash -> {
            AddressData addressData = Optional.ofNullable(addressHashesToAddressesFromDB.get(hash)).orElse(addressHashesToAddressesFromStorage.get(hash));
            if (addressHashesToAddressesFromStorage.containsKey(hash) && addressData == null) {
                requestedAddressHashes.put(new RequestedAddressHashData(hash));
            }
            orderedResponse.put(hash, addressData);
        });
        return orderedResponse;
    }

    private HashMap<Hash, AddressData> populateAndRemoveFoundAddresses(List<Hash> addressesHashes) {
        HashMap<Hash, AddressData> addressesFoundInDb = new HashMap<>();

        addressesHashes.removeIf(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if (addressData != null) {
                addressesFoundInDb.put(addressHash, addressData);
                return true;
            }
            RequestedAddressHashData requestedAddressHashData = requestedAddressHashes.getByHash(addressHash);
            if (validateRequestedAddressHashExistsAndRelevant(requestedAddressHashData)) {
                addressesFoundInDb.put(addressHash, null);
                return true;
            }
            return false;
        });

        return addressesFoundInDb;
    }

    private GetHistoryAddressesResponse getAddressesFromStorage(List<Hash> addressesHashes) {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressesHashes);
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);
        GetHistoryAddressesResponse getHistoryAddressesResponse = null;
        try {
            getHistoryAddressesResponse = addressStorageConnector.retrieveFromStorage(storageServerAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("{}: {}", e.getClass().getName(), ((SerializableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage());
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
        return getHistoryAddressesResponse;
    }

    private Optional<ResponseEntity<IResponse>> validateStorageResponse(GetHistoryAddressesResponse getHistoryAddressesResponseFromStorageNode) {
        if (getHistoryAddressesResponseFromStorageNode == null) {
            return Optional.of(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(STORAGE_RESPONSE_VALIDATION_ERROR, STATUS_ERROR)));
        }

        if (!getHistoryAddressesResponseCrypto.verifySignature(getHistoryAddressesResponseFromStorageNode)) {
            return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(STORAGE_INVALID_SIGNATURE, STATUS_ERROR)));
        }

        if (!validationService.validateGetAddressesResponse(getHistoryAddressesResponseFromStorageNode)) {
            return Optional.of(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(STORAGE_RESPONSE_VALIDATION_ERROR, STATUS_ERROR)));
        }
        return Optional.empty();
    }

    @Override
    public boolean validateRequestedAddressHashExistsAndRelevant(RequestedAddressHashData requestedAddressHashData) {
        return requestedAddressHashData != null;
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        requestedAddressHashes.delete(addressData);
    }
}
