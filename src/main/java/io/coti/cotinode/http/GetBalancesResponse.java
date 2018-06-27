package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

@Data
public class GetBalancesResponse extends Response {

    private List<AbstractMap.SimpleEntry<Hash, Double>> amounts;

    public GetBalancesResponse() {
        super();
        amounts = Arrays.asList(
                new AbstractMap.SimpleEntry<>(new Hash("ABCD"), 21.2));
    }
}
