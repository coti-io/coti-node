package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class HealthCheckService {

    private static final String NODE_HASH_END_POINT = "/nodeHash";
    private static final int RETRY_INTERVAL_IN_SECONDS = 20;
    private static final int MAX_NUM_OF_TRIES = 3;
    private final INodeManagementService nodesService;
    private final ActiveNode activeNode;
    private final RestTemplate restTemplate;

    @Autowired
    public HealthCheckService(INodeManagementService nodesService, ActiveNode activeNode, RestTemplate restTemplate) {
        this.nodesService = nodesService;
        this.activeNode = activeNode;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void neighborsHealthCheck() {
        try {
            boolean networkChanged = false;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Callable<Boolean>> checkNodesListTasks = new ArrayList<>(3);
            checkNodesListTasks.add((() -> checkNodesList(nodesService.getAllNetworkData().getDspNetworkNodesList())));
            checkNodesListTasks.add((() -> checkNodesList(nodesService.getAllNetworkData().getTrustScoreNetworkNodesList())));
            checkNodesListTasks.add((() -> checkNodesList(nodesService.getAllNetworkData().getFullNetworkNodesList())));
            List<Future<Boolean>> checkNodesListFutures = executorService.invokeAll(checkNodesListTasks);
            for (Future<Boolean> future : checkNodesListFutures) {
                if (future.get() == true) {
                    networkChanged = true;
                }
            }
            executorService.shutdown();

            NetworkNodeData zerospendNetworkNodeData = nodesService.getAllNetworkData().getZerospendServer();
            if (!isNodeConnected(zerospendNetworkNodeData)) {
                log.info("{} of address is about to be deleted", zerospendNetworkNodeData.getNodeType(),
                        zerospendNetworkNodeData.getHttpFullAddress());
                deleteNodeRecord(zerospendNetworkNodeData);
                nodesService.getAllNetworkData().setZerospendServer(null);
                networkChanged = true;
            }
            if (networkChanged) {
                nodesService.updateNetworkChanges();
            }
        } catch (Exception ex) {
            log.error("Exception in health check: ", ex);
        }
    }

    private boolean isNodeConnected(NetworkNodeData networkNodeDataToCheck) {
        if (networkNodeDataToCheck == null) {
            return true;
        }
        int tries = 0;
        while (tries < MAX_NUM_OF_TRIES) {
            try {
                if (tries != 0) {
                    log.info("Waiting {} seconds for #{} retry to {} of address {} healthcheck",
                            RETRY_INTERVAL_IN_SECONDS, tries, networkNodeDataToCheck.getNodeType(),
                            networkNodeDataToCheck.getHttpFullAddress());
                    TimeUnit.SECONDS.sleep(RETRY_INTERVAL_IN_SECONDS);
                }
                Hash nodeHash = restTemplate.getForObject(networkNodeDataToCheck.getHttpFullAddress() + NODE_HASH_END_POINT, Hash.class);
                if (nodeHash != null) {
                    log.debug("{} of address {} and port {} is responding to healthcheck.",
                            networkNodeDataToCheck.getNodeType(), networkNodeDataToCheck.getAddress(), networkNodeDataToCheck.getHttpPort());
                    return true;
                }
            } catch (Exception ex) {
                log.error("Exception in health check to {} . this was the #{} attempt out of {}. Err: {}",
                        networkNodeDataToCheck.getHttpFullAddress(), tries + 1, MAX_NUM_OF_TRIES, ex.getMessage());
            } finally {
                tries++;
            }
        }
        return false;
    }

    private boolean checkAndDeleteNodeIfNeeded(NetworkNodeData networkNodeData, List<NetworkNodeData> nodesToRemove) {
        if (!isNodeConnected(networkNodeData)) {
            deleteNodeRecord(networkNodeData);
            nodesToRemove.add(networkNodeData);
            return true;
        }
        return false;
    }


    private synchronized boolean checkNodesList(List<NetworkNodeData> nodesList) {
        boolean networkChanged = false;
        try {
            List<NetworkNodeData> nodesToRemove = new LinkedList<>();
            if (!nodesList.isEmpty()) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                List<Callable<Boolean>> nodeCheckTasks = new ArrayList<>(nodesList.size());
                Iterator<NetworkNodeData> iterator = nodesList.iterator();
                while (iterator.hasNext()) {
                    NetworkNodeData networkNodeData = iterator.next();
                    nodeCheckTasks.add(() -> checkAndDeleteNodeIfNeeded(networkNodeData, nodesToRemove));
                }
                List<Future<Boolean>> checkNodeFutures = executorService.invokeAll(nodeCheckTasks);
                for (Future<Boolean> future : checkNodeFutures) {
                    if (future.get() == true) {
                        networkChanged = true;
                    }
                }
                executorService.shutdown();
            }
            nodesToRemove.forEach(networkNode -> nodesService.getAllNetworkData().removeNode(networkNode));
        } catch (Exception ex) {
            log.error("Error while checking nodeList", ex);
        }
        return networkChanged;
    }


    private void deleteNodeRecord(NetworkNodeData networkNodeData) {
        log.info("{} of address {} and port {}  is about to be deleted", networkNodeData.getNodeType(), networkNodeData.getAddress(), networkNodeData.getHttpPort());
        nodesService.insertDeletedNodeRecord(networkNodeData);
        ActiveNodeData activeNodeData = new ActiveNodeData(networkNodeData.getHash(), null);
        activeNode.put(activeNodeData);
    }


}
