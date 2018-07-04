package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.List;

@Data
public class GetBalancesResponse extends Response {

    private List<AbstractMap.SimpleEntry<Hash, BigDecimal>> amounts;

    public GetBalancesResponse() {
        super("Balance details");
    }
}
