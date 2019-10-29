package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.min;

@Service
@Slf4j
public class HealthCheckService implements IHealthCheckService {

    private static final String NODE_HASH_END_POINT = "/nodeHash";
    private static final int RETRY_INTERVAL_IN_SECONDS = 20;
    private static final int MAX_NUM_OF_TRIES = 3;
    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private INetworkService networkService;
    private Thread healthCheckThread;

    @Override
    public void init() {
        healthCheckThread = new Thread(() -> nodesHealthCheck());
        healthCheckThread.start();
    }

    public void nodesHealthCheck() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(5000);
                boolean networkChanged = checkNodesList(networkService.getNetworkNodeDataList());
                if (networkChanged) {
                    nodeManagementService.propagateNetworkChanges();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Exception in health check: ", e);
            }
        }
    }

    private boolean isNodeConnected(NetworkNodeData networkNodeDataToCheck) {
        if (networkNodeDataToCheck == null) {
            return true;
        }
        int tries = 0;
        while (tries < MAX_NUM_OF_TRIES) {
            if (Thread.currentThread().isInterrupted()) {
                return true;
            }
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return true;
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
            if (networkNodeData.getNodeType() != NodeType.FinancialServer) {
                deleteNodeRecord(networkNodeData);
                nodesToRemove.add(networkNodeData);
                return true;
            }
        }
        return false;
    }

    private boolean checkNodesList(List<NetworkNodeData> nodesList) {
        boolean networkChanged = false;
        if (!nodesList.isEmpty()) {
            ExecutorService executorService = Executors.newFixedThreadPool(min(20, nodesList.size()));
            List<NetworkNodeData> nodesToRemove = new LinkedList<>();
            try {
                List<Callable<Boolean>> nodeCheckTasks = new ArrayList<>(nodesList.size());
                Iterator<NetworkNodeData> iterator = nodesList.iterator();
                while (iterator.hasNext()) {
                    NetworkNodeData networkNodeData = iterator.next();
                    nodeCheckTasks.add(() -> checkAndDeleteNodeIfNeeded(networkNodeData, nodesToRemove));
                }
                List<Future<Boolean>> checkNodeFutures = executorService.invokeAll(nodeCheckTasks);
                for (Future<Boolean> future : checkNodeFutures) {
                    if (future.get()) {
                        networkChanged = true;
                    }
                }
                executorService.shutdown();
                nodesToRemove.forEach(networkNode -> networkService.removeNode(networkNode));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdown();
            } catch (Exception e) {
                log.error("Error while checking nodeList", e);
            }
        }
        return networkChanged;
    }

    private void deleteNodeRecord(NetworkNodeData networkNodeData) {
        log.info("Deleting {} of address {} and port {}", networkNodeData.getNodeType(), networkNodeData.getAddress(), networkNodeData.getHttpPort());
        nodeManagementService.insertDeletedNodeRecord(networkNodeData);
        activeNodes.delete(networkNodeData);
    }

    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        healthCheckThread.interrupt();
        try {
            healthCheckThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted shutdown health check service");
        }
    }

}
