package io.coti.basenode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.interfaces.IIpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

@Component
@Slf4j
public class IpService implements IIpService {

    private String myIp;

    @PostConstruct
    public void init() {
        myIp = getIp();
        log.info("My external ip: {}", myIp);
    }

    public String getIp() {
        String ip = "";
        try {
            URL whatIsMyIp = new URL("http://checkip.amazonaws.com");
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        whatIsMyIp.openStream()));
                ip = in.readLine();
                return ip;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("io error:", e);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in calling checkip sevice", ex);
        }
        return ip;
    }

    public void modifyNetworkDetailsIfNeeded(NetworkDetails networkDetails) {
        modifyListIfNeeded(networkDetails.getDspNetworkNodesList());
        modifyListIfNeeded(networkDetails.getTrustScoreNetworkNodesList());
        modifyListIfNeeded(networkDetails.getFullNetworkNodesList());
        NetworkNodeData zeroSpend = networkDetails.getZerospendServer();
        if (zeroSpend != null) {
            zeroSpend.setAddress(getIpOfRemoteServer(zeroSpend.getAddress()));
        }
    }

    private void modifyListIfNeeded(List<NetworkNodeData> networkNodeDataList) {
        networkNodeDataList.forEach(node -> node.setAddress(getIpOfRemoteServer(node.getAddress())));
}

    public String getIpOfRemoteServer(String remoteServerIp) {
        if (remoteServerIp.equals(myIp)) {
            return "localhost";
        }
        return remoteServerIp;
    }
}
