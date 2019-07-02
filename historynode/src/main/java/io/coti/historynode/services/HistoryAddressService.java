package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.historynode.crypto.AddressesRequestCrypto;
import io.coti.historynode.crypto.HistoryAddressCrypto;
import io.coti.historynode.http.GetAddressesRequest;
import io.coti.historynode.http.GetAddressesResponse;
import io.coti.historynode.services.interfaces.IHistoryAddressService;
import io.coti.historynode.services.interfaces.IStorageConnector;
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
    protected IStorageConnector storageConnector;
    @Autowired
    private Addresses addresses;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;
    @Autowired
    private HistoryAddressCrypto addressCrypto;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        endpoint = "/transactionsAddresses";
    }

    public ResponseEntity<IResponse> getAddresses(GetAddressesRequest getAddressesRequest) {
        if(!addressCrypto.verifyGetAddressRequestSignatureMessage(getAddressesRequest)) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new Response(BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        Set<Hash> addressesHashes = getAddressesRequest.getAddressesHash();
        Map<Hash,AddressData> addressesFoundInRocksDb = populateAndRemoveFoundAddresses(addressesHashes);
        ResponseEntity<IResponse> storageResponse = getAddressesFromStorage(addressesHashes);

        if(!addressCrypto.getAddressResponseSignatureMessage((GetAddressesResponse) storageResponse.getBody())) {
            return generateResponse(HttpStatus.UNAUTHORIZED, new Response(BaseNodeHttpStringConstants.INVALID_SIGNATURE, BaseNodeHttpStringConstants.STATUS_ERROR));
        }

        addressesFoundInRocksDb.putAll(((GetAddressesResponse)storageResponse.getBody()).getAddressHashesToAddresses());
        //TODO 7/2/2019 astolia: What message should be set as argument for GetAddressesResponse?
        return generateResponse(HttpStatus.OK, new GetAddressesResponse(addressesFoundInRocksDb, BaseNodeHttpStringConstants.STATUS_SUCCESS, BaseNodeHttpStringConstants.STATUS_SUCCESS));
    }

    private Map<Hash,AddressData> populateAndRemoveFoundAddresses(Set<Hash> addressesHashes){
        Map<Hash,AddressData> addressesFoundInRocksDb = new HashMap<>();
        addressesHashes.forEach(addressHash -> {
            AddressData addressData = addresses.getByHash(addressHash);
            if(addressData != null){
                addressesFoundInRocksDb.put(addressHash,addressData);
                addressesHashes.remove(addressHash);
            }
        });
        return addressesFoundInRocksDb;
    }

    private ResponseEntity<IResponse> getAddressesFromStorage(Set<Hash> addressesHashes) {
        GetAddressesRequest getAddressesRequest = new GetAddressesRequest(addressesHashes);
        //TODO 7/1/2019 astolia: sign the message
        return storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getAddressesRequest);
    }

    private ResponseEntity<IResponse> generateResponse(HttpStatus httpStatus, Response response){
        return ResponseEntity.status(httpStatus).body(response);
    }

//    public ResponseEntity<IResponse> getAddress(GetAddressRequest getAddressRequest) {
////        if(!AddressRequestCrypto.verifySignature(getAddressRequest)) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
////        }
//
//        Hash addressHash = getAddressRequest.getAddressesHash();
//        AddressData addressData = addresses.getByHash(addressHash);
//        if(addressData != null){
//            return ResponseEntity
//                    .status(HttpStatus.OK)
//                    .body(new GetAddressResponse(addressHash,addressData));
//
//        }
//
//        // else call endpoint form storage
//        ResponseEntity<IResponse> response = getAddressFromStorage(addressHash);
//        return response;
//    }


    private ResponseEntity<IResponse> getAddressFromStorage(Hash address) {
        GetEntityRequest getEntityRequest = new GetEntityRequest(address);
        return storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getEntityRequest);
    }

}
