package io.coti.common.http;

import io.coti.common.crypto.CryptoUtils;
import io.coti.common.data.Hash;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetBalancesResponseEntity {
    private String address;
    private BigDecimal amount;

    public GetBalancesResponseEntity(Hash hash, BigDecimal amount) {
        this.address = CryptoUtils.bytesToHex(hash.getBytes()) ;
        this.amount = amount;
    }
}
