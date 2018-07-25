package io.coti.common.http;

import io.coti.common.data.Hash;
import lombok.Data;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GetBalancesResponse extends BaseResponse {

    Map<String, BigDecimal> addressesBalance;

    public GetBalancesResponse() {
        addressesBalance = new HashMap<String, BigDecimal>();
    }

    public void addAddressBalanceToResponse(Hash address, BigDecimal balance) {
        addressesBalance.put(address.toHexString(), balance);
    }
}
