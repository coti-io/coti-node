package io.coti.zerospend.monitor.interfaces;

import io.coti.common.data.Hash;

import java.util.Map;

public interface IAccessMonitor {

    boolean validateAccessEvent(Hash transactionHash);

    Map<Hash, Integer> getAccessCounters();

}
