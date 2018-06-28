package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IBalanceService {

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    boolean insertToUnconfirmedTransactions(ConfirmationData confirmationData);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    Map<Hash, Double> getBalanceMap();

    Map<Hash, Double> getPreBalanceMap();
}

