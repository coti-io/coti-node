package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.InetAddress;

@Data
@EqualsAndHashCode(callSuper = true)
public class NodeInformationResponse extends Response {

    private InetAddress ipV4Address;
}
