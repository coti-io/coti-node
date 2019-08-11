package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AddressBalance {

    public Map<Hash, BigDecimal> addressBalance;
    public Map<Hash, BigDecimal> addressPreBalance;

    public AddressBalance(Map<Hash, BigDecimal> addressBalance, Map<Hash, BigDecimal> addressPreBalance) {
        this.addressBalance = addressBalance;
        this.addressPreBalance = addressPreBalance;
    }
}
