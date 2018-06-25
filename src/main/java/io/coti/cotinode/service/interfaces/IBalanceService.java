package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.ConfirmationData;

import java.util.List;
import java.util.Map;

public interface IBalanceService {

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    void insertIntoUnconfirmedDBandAddToTccQeueue(ConfirmationData confirmationData);
}
