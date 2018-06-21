package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.UnconfirmedTransactionData;

import java.util.List;
import java.util.Map;

public interface IBalanceService {

    public boolean inMemorySync(List<Map.Entry<Hash, Double>> pairList);

    public void dbSync(UnconfirmedTransactionData unconfirmedTransactionData);
}