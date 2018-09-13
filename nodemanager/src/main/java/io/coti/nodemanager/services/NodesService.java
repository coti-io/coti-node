package io.coti.nodemanager.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NodesService {
    private String zeroSpendServerAddress;

    public void newZeroSpendServer(String zeroSpendServerAddress) {
        log.info("Zero Spend address received: {}", zeroSpendServerAddress);
        this.zeroSpendServerAddress = zeroSpendServerAddress;
    }

    public void newDspNode(String dspNodeAddress) {
        log.info("Zero Spend address received: {}", dspNodeAddress);
        this.zeroSpendServerAddress = dspNodeAddress;
    }

    public String getZeroSpendAddress() {
        return zeroSpendServerAddress;
    }
}
