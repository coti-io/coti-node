package io.coti.zero_spend.monitor;

import io.coti.common.data.Hash;

public interface MonitorAccess {


    public void accessEvent(Hash transactionHash);


}
