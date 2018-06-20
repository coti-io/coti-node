package io.coti.cotinode.http;

import lombok.Data;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

@Data
public class GetBalancesResponse extends Response {

    private List<AbstractMap.SimpleEntry<String, Double>> amounts;

    public GetBalancesResponse() {
        super();
        amounts = Arrays.asList(
                new AbstractMap.SimpleEntry<>("ABCD", 21.2));
    }
}
