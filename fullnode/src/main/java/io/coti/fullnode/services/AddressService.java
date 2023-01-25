package io.coti.fullnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.*;
import io.coti.basenode.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static io.coti.fullnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class AddressService extends BaseNodeAddressService {

    @Override
    public Boolean addAddress(Hash addressHash) {
        AddressData addressData = new AddressData(addressHash);

        if (Boolean.FALSE.equals(super.addNewAddress(addressData))) {
            return Boolean.FALSE;
        }

        networkService.sendDataToConnectedDspNodes(addressData);
        continueHandleGeneratedAddress(addressData);
        return Boolean.TRUE;
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        webSocketSender.notifyGeneratedAddress(addressData.getHash());
    }

    @Override
    public AddressesExistsResponse addressesCheckExistenceAndRequestHistoryNode(AddressBulkRequest addressRequest) {

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

        if (!addressHashes.isEmpty()) {
            fillUnknownAddressesFromHistoryResponse(addressHashes, addressHashToFoundStatusMap, historyNodeHttpAddress);
        }

        return new AddressesExistsResponse(addressHashToFoundStatusMap);
    }

    private void fillUnknownAddressesFromHistoryResponse(List<Hash> addressHashesToFindInHistoryNode, Map<String, Boolean> addressHashesToFoundStatus, String historyNodeHttpAddress) {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashesToFindInHistoryNode);
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);

        RestTemplate restTemplate = new RestTemplate();
        Map<Hash, AddressData> historyHashToAddressMap = new HashMap<>();
        try {
            GetHistoryAddressesResponse getHistoryAddressesResponse = restTemplate.postForObject(historyNodeHttpAddress + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class);
            if (getHistoryAddressesResponse != null && validateHistoryResponse(getHistoryAddressesResponse) && getHistoryAddressesResponse.getAddressHashesToAddresses() != null) {
                historyHashToAddressMap.putAll(getHistoryAddressesResponse.getAddressHashesToAddresses());
            }

        } catch (HttpStatusCodeException e) {
            log.error("{}: {}", e.getClass().getName(), ((SerializableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage());
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }

        addressHashesToFindInHistoryNode.forEach(addressHash -> {
            if (historyHashToAddressMap.isEmpty()) {
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

    private String getHistoryNodeHttpAddress() {
        Map<Hash, NetworkNodeData> historyNodeMap = networkService.getMapFromFactory(NodeType.HistoryNode);
        Optional<NetworkNodeData> historyNode = historyNodeMap.values().stream().findFirst();
        return historyNode.map(NetworkNodeData::getHttpFullAddress).orElse(null);
    }
}
