package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.AddressBalance;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenBalancesResponse extends BaseResponse {

    private Map<Hash, Map<Hash, AddressBalance>> tokenBalances;

    public GetTokenBalancesResponse(Map<Hash, Map<Hash, AddressBalance>> tokenBalances) {
        this.tokenBalances = tokenBalances;
    }
}
