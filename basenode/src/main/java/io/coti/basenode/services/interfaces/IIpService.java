package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NetworkDetails;

public interface IIpService {
    String getIpOfRemoteServer(String remoteServerIp);

    String getIp();

    void modifyNetworkDetailsIfNeeded(NetworkDetails networkDetails);
}
