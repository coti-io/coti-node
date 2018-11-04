package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.interfaces.IIpService;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthCheckService {

    private static final String NODE_HASH_END_POINT = "/nodeHash";
    private static final int RETRY_INTERVAL_IN_SECONDS = 20;
    private static final int NUM_OF_RETRIES = 3;
    @Autowired
    private INodesManagementService nodesService;

    @Autowired
    private IIpService ipService;

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void healthCheckNeighbors() {
        boolean networkChanged = false;
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getDspNetworkNodesList(), networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getTrustScoreNetworkNodesList(), networkChanged);
        networkChanged = checkNodesList(nodesService.getAllNetworkData().getFullNetworkNodesList(), networkChanged);
        NetworkNodeData zerospendNetworkNodeData = nodesService.getAllNetworkData().getZerospendServer();
        if (!checkNode(zerospendNetworkNodeData)) {
            log.info("{} of address is about to be deleted", zerospendNetworkNodeData.getNodeType(),
                    zerospendNetworkNodeData.getHttpFullAddress());
            nodesService.insertDeletedNodeRecord(zerospendNetworkNodeData);
            nodesService.getAllNetworkData().setZerospendServer(null);
            networkChanged = true;
        }
        if (networkChanged) {
            nodesService.updateNetworkChanges();
        }
    }

    private boolean checkNode(NetworkNodeData networkNodeDataToCheck) {
        if (networkNodeDataToCheck == null) {
            return true;
        }
        RestTemplate restTemplate = new RestTemplate();
        int tries = 0;
        while (tries < NUM_OF_RETRIES) {
            try {
                if (tries != 0) {
                    log.info("Waiting {} seconds for # {} retry to {} of address {} healthcheck",
                            RETRY_INTERVAL_IN_SECONDS, tries, networkNodeDataToCheck.getNodeType(),
                            networkNodeDataToCheck.getHttpFullAddress());
                    TimeUnit.SECONDS.sleep(RETRY_INTERVAL_IN_SECONDS);
                }
                Hash nodeHash = restTemplate.getForObject( "http://" + getModifiedFullAddressIfNeeded(networkNodeDataToCheck.getAddress())
                        + ":" + networkNodeDataToCheck.getHttpPort() + NODE_HASH_END_POINT, Hash.class);
                if (nodeHash != null) {
                    log.debug("{} of address {} and port {} is responding to healthcheck.",
                            networkNodeDataToCheck.getNodeType(), networkNodeDataToCheck.getAddress(), networkNodeDataToCheck.getHttpPort());
                    return true;
                }
            } catch (Exception ex) {
                log.error("Exception in health check to {} . this was the #{} attempt out of {}. Err: {}",
                        networkNodeDataToCheck.getHttpFullAddress(), tries + 1, NUM_OF_RETRIES, ex.getMessage());
            } finally {
                tries++;
            }
        }
        return false;
    }

    private String getModifiedFullAddressIfNeeded(String externalServerAddress){
        return ipService.getIpOfRemoteServer(externalServerAddress);
    }


    private synchronized boolean checkNodesList(List<NetworkNodeData> nodesList, boolean networkChanged) {
        List<NetworkNodeData> nodesToRemove = new LinkedList<>();
        if (nodesList.size() > 0) {
            Iterator<NetworkNodeData> iterator = nodesList.iterator();
            while (iterator.hasNext()) {
                NetworkNodeData networkNodeData = iterator.next();
                if (!checkNode(networkNodeData)) {
                    log.info("{} of address {} and port {}  is about to be deleted", networkNodeData.getNodeType(), networkNodeData.getAddress(), networkNodeData.getHttpPort());
                    nodesService.insertDeletedNodeRecord(networkNodeData);
                    nodesToRemove.add(networkNodeData);
                    networkChanged = true;
                }
            }
        }
        nodesToRemove.forEach(networkNode -> nodesService.getAllNetworkData().removeNode(networkNode));
        return networkChanged;
    }


}
