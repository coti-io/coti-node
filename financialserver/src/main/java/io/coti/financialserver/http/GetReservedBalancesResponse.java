package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.ReservedBalanceResponseData;
import lombok.Data;

import java.util.List;

@Data
public class GetReservedBalancesResponse extends BaseResponse {
    List<ReservedBalanceResponseData> lockupBalances;

    public GetReservedBalancesResponse(List<ReservedBalanceResponseData> reservedBalances) {
        lockupBalances = reservedBalances;
    }
}
