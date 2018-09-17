package io.coti.nodemanager.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NodesService {
    private String zeroSpendServerAddress;
    private List<String> dspNodeAddresses = new ArrayList<>();

    public void newDspNode(String dspNodeAddress) {
        log.info("Dsp Node address received: {}", dspNodeAddress);
        this.dspNodeAddresses.add(dspNodeAddress);
    }

    public void newZeroSpendServer(String zeroSpendServerAddress) {
        log.info("Zero Spend address received: {}", zeroSpendServerAddress);
        this.zeroSpendServerAddress = zeroSpendServerAddress;
    }

    public List<String> getDsps() {
        return dspNodeAddresses;
    }

    public String getZeroSpendAddress() {
        return zeroSpendServerAddress;
    }
}
