package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NetworkData;

public interface INetworkService {

    void handleNetworkChanges(NetworkData networkData);

    NetworkData getNetworkData();

    void saveNetwork(NetworkData networkData);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    void connectToCurrentNetwork();
}
