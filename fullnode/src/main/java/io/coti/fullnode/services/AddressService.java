package io.coti.fullnode.services;

import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.fullnode.data.RequestedAddressHashData;
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.model.RequestedAddressHashes;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {

    private final int TRUSTED_RESULT_MAX_DURATION_IN_MILLIS = 600_000;

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
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;

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
        LinkedHashMap<String, Boolean> addressHashToFoundStatusMap = new LinkedHashMap<>();

        String historyNodeHttpAddress = getHistoryNodeHttpAddress();
        addressHashes.removeIf(addressHash -> {
            if (addresses.getByHash(addressHash) != null) {
                addressHashToFoundStatusMap.put(addressHash.toHexString(), Boolean.TRUE);
                return true;
            }
            RequestedAddressHashData requestedAddressHashData = requestedAddressHashes.getByHash(addressHash);
            if (validateRequestedAddressHashExistsAndRelevant(requestedAddressHashData) || historyNodeHttpAddress == null) {
                addressHashToFoundStatusMap.put(addressHash.toHexString(), Boolean.FALSE);
                return true;
            }
            addressHashToFoundStatusMap.put(addressHash.toHexString(), null);
            return false;
        });

        if (addressHashes.size() > 0) {
            fillUnknownAddressesFromHistoryResponse(addressHashes, addressHashToFoundStatusMap, historyNodeHttpAddress);
        }

        return new AddressesExistsResponse(addressHashToFoundStatusMap);
    }

    private void fillUnknownAddressesFromHistoryResponse(List<Hash> addressHashesToFindInHistoryNode, Map<String, Boolean> addressHashesToFoundStatus, String historyNodeHttpAddress) {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashesToFindInHistoryNode);
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);

        RestTemplate restTemplate = new RestTemplate();

        Map<Hash, AddressData> historyHashToAddressMap = null;
        try {
            GetHistoryAddressesResponse getHistoryAddressesResponse = restTemplate.postForEntity(historyNodeHttpAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class).getBody();
            if (validateHistoryResponse(getHistoryAddressesResponse)) {
                historyHashToAddressMap.putAll(getHistoryAddressesResponse.getAddressHashesToAddresses());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("{}: {}", e.getClass().getName(), ((SerializableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage());
        }


        addressHashesToFindInHistoryNode.forEach(addressHash -> {
            if (historyHashToAddressMap == null) {
                addressHashesToFoundStatus.put(addressHash.toString(), Boolean.FALSE);
                return;
            }
            Boolean isAddressUsed;
            AddressData addressData = historyHashToAddressMap.get(addressHash);
            if (addressData == null) {
                isAddressUsed = Boolean.FALSE;
                requestedAddressHashes.put(new RequestedAddressHashData(addressHash));
            } else {
                isAddressUsed = Boolean.TRUE;
                addresses.put(addressData);
            }
            addressHashesToFoundStatus.put(addressHash.toString(), isAddressUsed);
        });
    }

    private boolean validateHistoryResponse(GetHistoryAddressesResponse getHistoryAddressesResponse) {
        if (!getHistoryAddressesResponseCrypto.verifySignature(getHistoryAddressesResponse)) {
            log.error("Signature verification of history address response failed.");
            return false;
        }
        return true;
    }

    private boolean validateRequestedAddressHashExistsAndRelevant(RequestedAddressHashData requestedAddressHashData) {
        if (requestedAddressHashData != null) {
            long diffInMilliSeconds = Math.abs(Duration.between(Instant.now(), requestedAddressHashData.getLastUpdateTime()).toMillis());
            return diffInMilliSeconds <= TRUSTED_RESULT_MAX_DURATION_IN_MILLIS;
        }
        return false;
    }

    private String getHistoryNodeHttpAddress() {
        Map<Hash, NetworkNodeData> historyNodeMap = networkService.getMapFromFactory(NodeType.HistoryNode);
        if (historyNodeMap.isEmpty()) {
            return null;
        }
        return historyNodeMap.values().stream().findFirst().get().getHttpFullAddress();
    }
}
