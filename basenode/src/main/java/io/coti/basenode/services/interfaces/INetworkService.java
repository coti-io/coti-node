package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface INetworkService {

    void handleNetworkChanges(NetworkDetails networkDetails);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    void addListToSubscriptionAndNetwork(Collection<NetworkNodeData> nodeDataList);

}
