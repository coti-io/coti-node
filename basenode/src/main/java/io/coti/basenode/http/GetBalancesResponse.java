package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.AddressBalanceData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetBalancesResponse extends BaseResponse {

    private Map<String, AddressBalanceData> addressesBalance;

    public GetBalancesResponse() {
        addressesBalance = new HashMap<>();
    }

    public void addAddressBalanceToResponse(Hash address, BigDecimal balance, BigDecimal preBalance) {
        addressesBalance.put(address.toHexString(), new AddressBalanceData(balance, preBalance));
    }
}
