package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;

public interface IBalanceService {

    GetBalancesResponse getBalances(GetBalancesRequest request);
}
