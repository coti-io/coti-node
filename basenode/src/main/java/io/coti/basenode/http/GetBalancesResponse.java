package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.AddressBalance;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class GetBalancesResponse extends BaseResponse {

    Map<String, AddressBalance> addressesBalance;

    public GetBalancesResponse() {
        addressesBalance = new HashMap<>();
    }

    public void addAddressBalanceToResponse(Hash address, BigDecimal balance, BigDecimal preBalance) {
        addressesBalance.put(address.toHexString(), new AddressBalance(balance, preBalance));
    }
}
