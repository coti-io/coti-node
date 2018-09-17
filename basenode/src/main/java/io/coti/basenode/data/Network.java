package io.coti.basenode.data;

import lombok.Data;

import java.util.List;

@Data
public class Network {
    public List<Node> dspNodes;
    public List<Node> fullNodes;
    public List<Node> trustScoreNodes;
    Node zerospendServer;
}