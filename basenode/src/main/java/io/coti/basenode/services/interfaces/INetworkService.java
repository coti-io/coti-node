package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NetworkDetails;

public interface INetworkService {

    void handleNetworkChanges(NetworkDetails networkDetails);

    NetworkDetails getNetworkDetails();

    void saveNetwork(NetworkDetails networkDetails);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);
}
