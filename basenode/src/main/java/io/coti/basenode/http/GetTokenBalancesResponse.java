package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.AddressBalance;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GetTokenBalancesResponse extends BaseResponse {

    private Map<Hash, Map<Hash, AddressBalance>> tokenToAddressesBalance;

    public GetTokenBalancesResponse() {
        this.tokenToAddressesBalance = new HashMap<>();
    }
}
