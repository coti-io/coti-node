package io.coti.fullnode.services;

import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.fullnode.data.RequestedAddressHashData;
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.model.RequestedAddressHashes;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {

    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private Addresses addresses;
    @Autowired
    private RequestedAddressHashes requestedAddressHashes;
    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @Value("${history.server.address}")
    private String historyServerAddress;

    private final int TRUSTED_RESULT_MAX_DURATION_IN_MILLIS = 600_000;

    public boolean addAddress(Hash addressHash) {
        AddressData addressData = new AddressData(addressHash);

        if (!super.addNewAddress(addressData)) {
            return false;
        }

        networkService.sendDataToConnectedDspNodes(addressData);
        continueHandleGeneratedAddress(addressData);
        return true;
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        webSocketSender.notifyGeneratedAddress(addressData.getHash());
    }

    public AddressesExistsResponse addressesExist(AddressBulkRequest addressRequest) {

        List<Hash> addressHashes = addressRequest.getAddresses();
        LinkedHashMap<String,Boolean> addressHashesToFoundStatus = new LinkedHashMap<>();

        //TODO 7/29/2019 astolia: make sure this is orderd
        addressHashes.removeIf( addressHash -> {
            if(addresses.getByHash(addressHash) != null){
                addressHashesToFoundStatus.put(addressHash.toHexString(), Boolean.TRUE);
                return true;
            }
            RequestedAddressHashData requestedAddressHashData = requestedAddressHashes.getByHash(addressHash);
            if(validateRequestedAddressHashExistsAndRelevant(requestedAddressHashData)){
                addressHashesToFoundStatus.put(addressHash.toHexString(), Boolean.FALSE);
                return true;
            }
            addressHashesToFoundStatus.put(addressHash.toHexString(), null);
            return false;
        });

        if(addressHashes.size() > 0){
            fillUnknownAddressesFromHistoryResponse(addressHashes, addressHashesToFoundStatus);
        }

        return new AddressesExistsResponse(addressHashesToFoundStatus);
    }

    private void fillUnknownAddressesFromHistoryResponse(List<Hash> addressHashesToFindInHistoryNode, Map<String,Boolean> addressHashesToFoundStatus){
        GetHistoryAddressesRequest getHistoryAddressesRequestToHistory = new GetHistoryAddressesRequest(addressHashesToFindInHistoryNode);
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequestToHistory);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GetHistoryAddressesResponse> historyResponse =  restTemplate.postForEntity(historyServerAddress + "/addresses", getHistoryAddressesRequestToHistory, GetHistoryAddressesResponse.class);

        if(!validateHistoryResponse(historyResponse)){
            //TODO 7/29/2019 astolia: is this correct? put false if response from history is bad?
            addressHashesToFindInHistoryNode.forEach( hash -> addressHashesToFoundStatus.put(hash.toHexString(),Boolean.FALSE));
            return;
        }

        historyResponse.getBody().getAddressHashesToAddresses().entrySet().forEach(
                entry -> {
                    Boolean isAddressUsed;
                    if(entry.getValue() == null){
                        isAddressUsed = Boolean.FALSE;
                        requestedAddressHashes.put(new RequestedAddressHashData(entry.getKey()));
                    }
                    else{
                        isAddressUsed = Boolean.TRUE;
                        addresses.put(entry.getValue());
                    }
                    addressHashesToFoundStatus.put(entry.getKey().toHexString(), isAddressUsed);
                });
    }

    private boolean validateHistoryResponse(ResponseEntity<GetHistoryAddressesResponse> historyResponse){
        GetHistoryAddressesResponse responseBody = historyResponse.getBody();
        if(!historyResponse.getStatusCode().is2xxSuccessful()){
            log.error(String.format("Unsuccessful status code {} for {} response"), historyResponse.getStatusCode(), responseBody.getClass().getSimpleName());
            return false;
        }

        if (!getHistoryAddressesResponseCrypto.verifySignature(responseBody)) {
            log.error(String.format("Signature verification of {} failed."), responseBody.getClass().getSimpleName());
            return false;
        }

        return true;
    }

    private boolean validateRequestedAddressHashExistsAndRelevant(RequestedAddressHashData requestedAddressHashData){
        if(requestedAddressHashData != null){
            long diffInMilliSeconds = Math.abs(Duration.between(Instant.now(), requestedAddressHashData.getLastUpdateTime()).toMillis());
            return diffInMilliSeconds <= TRUSTED_RESULT_MAX_DURATION_IN_MILLIS;
        }
        return false;
    }
}
