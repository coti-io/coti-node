package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Network;

public interface INetworkService {

    void handleNetworkChanges(Network network);

    Network getNetwork();

    void saveNetwork(Network network);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    void connectToCurrentNetwork();
}
