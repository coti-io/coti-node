package io.coti.basenode.services;

import io.coti.basenode.data.Network;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeNetworkService {
    private Network network;

    public void handleNetworkChanges(Network network){
        log.info("New network structure received: {}", network);
        this.network = network;
        connectToNetwork(network);
    }

    public Network getNetwork(){
        return network;
    }

    public void connectToNetwork(Network network){}
}
