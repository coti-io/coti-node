package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.Node;
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

    private static String nodeHashEndPoint = "/nodeHash";

    @Autowired
    private NodesService nodesService;


    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void healthCheckNeighbors() {
        boolean networkChanged = false;
        networkChanged = checkNodesList(nodesService.getAllNodes().dspNodes, networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNodes().trustScoreNodes, networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNodes().fullNodes, networkChanged);
        Node zerospendNode = nodesService.getAllNodes().getZerospendServer();
        if (!checkNode(zerospendNode)) {
            log.error("{} of address {} and port {}  is about to be deleted", zerospendNode.getNodeType(), zerospendNode.getAddress(), zerospendNode.getHttpPort());
            nodesService.getAllNodes().setZerospendServer(null);
            networkChanged = true;
        }
        if (networkChanged) {
            nodesService.updateNetworkChanges();
        }
    }

    private boolean checkNode(Node nodeToCheck) {
        if (nodeToCheck != null) {
            RestTemplate restTemplate = new RestTemplate();
            int tries = 0;
            while (tries < 3) {
                try {
                    if (tries > 0) {
                        TimeUnit.SECONDS.sleep(30);
                        log.info("Waiting for {} retry for {} of address {} and port {} healthcheck", tries,
                                nodeToCheck.getNodeType(), nodeToCheck.getAddress(), nodeToCheck.getHttpPort());
                    }
                    Hash nodeHash = restTemplate.getForObject(nodeToCheck.getHttpFullAddress() +
                            nodeHashEndPoint, Hash.class);

                    if (nodeHash != null) {
                        log.info("{} of address {} and port {} is responding to healthcheck.",
                                nodeToCheck.getNodeType(), nodeToCheck.getAddress(), nodeToCheck.getHttpPort());
                        return true;
                    }
                    tries++;
                    log.info("{} of address {} and port {} is not responding to healthcheck. num of tries {}",
                            nodeToCheck.getNodeType(), nodeToCheck.getAddress(), nodeToCheck.getHttpPort(), tries);
                } catch (Exception ex) {
                    tries++;
                    log.error("Exception in health check", ex);
                }
            }
            return false;
        }
        return true;
    }

    private synchronized boolean checkNodesList(List<Node> nodesList, boolean networkChanged) {
        List<String> nodesToRemove = new LinkedList<>();
        if (nodesList.size() > 0) {
            for(Node node : nodesList){
                if (!checkNode(node)) {
                    log.error("{} of address {} and port {}  is about to be deleted", node.getNodeType(), node.getAddress(), node.getHttpPort());
                    nodesToRemove.add(node.getHttpFullAddress());
                    networkChanged = true;
                }
            }
        }
        nodesList.removeIf(node -> nodesToRemove.contains(node.getHttpFullAddress()));
        return networkChanged;

    }


}
