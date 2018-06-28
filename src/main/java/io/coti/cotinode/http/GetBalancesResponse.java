package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import java.util.AbstractMap;
import java.util.List;

@Data
public class GetBalancesResponse extends Response {

    private List<AbstractMap.SimpleEntry<Hash, Double>> amounts;

    public GetBalancesResponse() {
        super();
    }
}
