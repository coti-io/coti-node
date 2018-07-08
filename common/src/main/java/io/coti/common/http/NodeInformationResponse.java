package io.coti.common.http;

import lombok.Data;

import java.net.InetAddress;

@Data
public class NodeInformationResponse extends Response {
    private InetAddress IpV4Address;
}
