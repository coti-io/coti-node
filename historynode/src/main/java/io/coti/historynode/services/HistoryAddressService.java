package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.historynode.crypto.AddressesRequestCrypto;
import io.coti.historynode.crypto.HistoryAddressCrypto;
import io.coti.historynode.services.interfaces.IHistoryAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Slf4j
@Service
public class HistoryAddressService extends EntityService implements IHistoryAddressService {
    @Autowired
    protected HistoryAddressStorageConnector storageConnector;
    @Autowired
    private Addresses addresses;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;
    @Autowired
    private HistoryAddressCrypto addressCrypto;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
    }

    public ResponseEntity<GetAddressesResponse> getAddresses(GetAddressesRequest getAddressesRequest) {
        if(!addressCrypto.verifyGetAddressRequestSignatureMessage(getAddressesRequest)) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new GetAddressesResponse(new HashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        Set<Hash> addressesHashes = getAddressesRequest.getAddressesHash();
        Map<Hash,AddressData> addressToAddressDataResponse = populateAndRemoveFoundAddresses(addressesHashes);
        ResponseEntity<GetAddressesResponse> storageResponse = getAddressesFromStorage(addressesHashes);

        if(!addressCrypto.getAddressResponseSignatureMessage((GetAddressesResponse) storageResponse.getBody())) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new GetAddressesResponse(new HashMap<>(),BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        addressToAddressDataResponse.putAll(((GetAddressesResponse)storageResponse.getBody()).getAddressHashesToAddresses());
        //TODO 7/2/2019 astolia: What message should be set as argument for GetAddressesResponse?
        return generateResponse(HttpStatus.OK, new GetAddressesResponse(addressToAddressDataResponse, BaseNodeHttpStringConstants.STATUS_SUCCESS, BaseNodeHttpStringConstants.STATUS_SUCCESS));
    }

    private Map<Hash,AddressData> populateAndRemoveFoundAddresses(Set<Hash> addressesHashes){
        Map<Hash,AddressData> addressesFoundInRocksDb = new HashMap<>();

        addressesHashes.forEach(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if(addressData != null){
                addressesFoundInRocksDb.put(addressHash,addressData);
            }
        });
        addressesFoundInRocksDb.keySet().forEach(addressHash -> addressesHashes.remove(addressHash));
        return addressesFoundInRocksDb;
    }

    private ResponseEntity<GetAddressesResponse> getAddressesFromStorage(Set<Hash> addressesHashes) {
        GetAddressesRequest getAddressesRequest = new GetAddressesRequest(addressesHashes);
        addressesRequestCrypto.signMessage(getAddressesRequest);
        return  storageConnector.postForObjects(storageServerAddress + "/addresses",getAddressesRequest, GetAddressesResponse.class);
//        return  storageConnector.postForObjects(storageServerAddress + "/addresses",getAddressesRequest);
    }

    private <T extends Response> ResponseEntity<T> generateResponse(HttpStatus httpStatus, T response){
        return ResponseEntity.status(httpStatus).body(response);
    }

    private ResponseEntity<IResponse> getAddressFromStorage(Hash address) {
        GetEntityRequest getEntityRequest = new GetEntityRequest(address);
        return storageConnector.getForObject(storageServerAddress + "/transactionsAddresses", getEntityRequest, ResponseEntity.class);
    }

    @Override
    protected void storeEntitiesByType(String s, AddEntitiesBulkRequest addEntitiesBulkRequest) {
        storageConnector.postForObjects(storageServerAddress + endpoint, addEntitiesBulkRequest);
    }
}
