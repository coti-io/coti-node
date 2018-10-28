package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthCheckService {

    private static final String NODE_HASH_END_POINT = "/nodeHash";

    @Autowired
    private NodesService nodesService;

    private static final int RETRY_INTERVAL_IN_SECONDS = 30;


    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void healthCheckNeighbors() {
        boolean networkChanged = false;
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getDspNetworkNodes(), networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getTrustScoreNetworkNodes(), networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getFullNetworkNodes(), networkChanged);
        NetworkNode zerospendNetworkNode = nodesService.getAllNetworkData().getZerospendServer();
        if (!checkNode(zerospendNetworkNode)) {
            log.info("{} of address is about to be deleted", zerospendNetworkNode.getNodeType(),
                    zerospendNetworkNode.getHttpFullAddress());
            nodesService.getAllNetworkData().setZerospendServer(null);
            networkChanged = true;
        }
        if (networkChanged) {
            nodesService.updateNetworkChanges();
        }
    }

    private boolean checkNode(NetworkNode networkNodeToCheck) {
        if (networkNodeToCheck != null) {
            RestTemplate restTemplate = new RestTemplate();
            int tries = 0;
            while (tries < 3) {
                try {
                    if (tries > 0) {
                        TimeUnit.SECONDS.sleep(RETRY_INTERVAL_IN_SECONDS);
                        log.info("Waiting {} seconds for # {} retry for {} of address {} and port {} healthcheck",
                                RETRY_INTERVAL_IN_SECONDS, tries, networkNodeToCheck.getNodeType(), networkNodeToCheck.getAddress(),
                                networkNodeToCheck.getHttpPort());
                    }
                    Hash nodeHash = restTemplate.getForObject(networkNodeToCheck.getHttpFullAddress() +
                            NODE_HASH_END_POINT, Hash.class);

                    if (nodeHash != null) {
                        log.info("{} of address {} and port {} is responding to healthcheck.",
                                networkNodeToCheck.getNodeType(), networkNodeToCheck.getAddress(), networkNodeToCheck.getHttpPort());
                        return true;
                    }
                    tries++;
                    log.info("{} of address {} and port {} is not responding to healthcheck. num of tries {}",
                            networkNodeToCheck.getNodeType(), networkNodeToCheck.getAddress(), networkNodeToCheck.getHttpPort(), tries);
                } catch (Exception ex) {
                    tries++;
                    log.error("Exception in health check", ex.getMessage());
                    log.debug("Exception in health check", ex);

                }
            }
            return false;
        }
        return true;
    }

    private synchronized boolean checkNodesList(List<NetworkNode> nodesList, boolean networkChanged) {
        List<NetworkNode> nodesToRemove = new LinkedList<>();
        if (nodesList.size() > 0) {
            for(NetworkNode networkNode : nodesList){
                if (!checkNode(networkNode)) {
                    log.info("{} of address {} and port {}  is about to be deleted", networkNode.getNodeType(), networkNode.getAddress(), networkNode.getHttpPort());
                    nodesToRemove.add(networkNode);
                    networkChanged = true;
                }
            }
        }
        nodesToRemove.forEach(networkNode -> nodesService.getAllNetworkData().removeNode(networkNode));
        return networkChanged;
    }


}
