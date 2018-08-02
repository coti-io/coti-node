package io.coti.zerospend.monitor.interfaces;

import io.coti.common.data.Hash;

import java.util.Map;

public interface IAccessMonitor {

    boolean insertAndValidateAccessEvent(Hash transactionHash);

    Map<Hash, Integer> getAccessCounters();

}
