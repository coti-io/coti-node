package io.coti.cotinode.http;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

@Data
public class GetBalancesResponse extends Response{

    public GetBalancesResponse(HttpStatus status, String message) {
        super(status, message);
        amounts = Arrays.asList(
                new AbstractMap.SimpleEntry<>("ABCD", 21.2));
    }

    private List<AbstractMap.SimpleEntry<String, Double>> amounts;
}
