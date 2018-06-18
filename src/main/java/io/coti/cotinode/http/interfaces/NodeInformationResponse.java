package io.coti.cotinode.http.interfaces;

import io.coti.cotinode.http.Response;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.net.InetAddress;

@Data
public class NodeInformationResponse extends Response {
    private InetAddress IpV4Address;

    public NodeInformationResponse(HttpStatus status, String message) {
        super(status, message);
    }

    public NodeInformationResponse(){
        super(HttpStatus.OK, "Node information");
    }
}
