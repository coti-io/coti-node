package io.coti.zero_spend.monitor.interfaces;

import io.coti.common.data.Hash;

public interface IMonitorAccess {

     boolean validateAccessEvent(Hash transactionHash);

}
