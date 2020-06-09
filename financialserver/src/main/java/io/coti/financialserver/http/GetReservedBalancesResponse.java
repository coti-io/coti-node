package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.ReservedBalanceResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetReservedBalancesResponse extends BaseResponse {

    private Set<ReservedBalanceResponseData> lockupBalances;

    public GetReservedBalancesResponse(Set<ReservedBalanceResponseData> reservedBalances) {
        lockupBalances = reservedBalances;
    }
}
