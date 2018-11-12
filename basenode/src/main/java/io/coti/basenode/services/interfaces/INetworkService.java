package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;

import java.util.List;

public interface INetworkService {

    void handleNetworkChanges(NetworkDetails networkDetails);

    NetworkDetails getNetworkDetails();

    void saveNetwork(NetworkDetails networkDetails);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    void addListToSubscriptionAndNetwork(List<NetworkNodeData> nodeDataList);

}
