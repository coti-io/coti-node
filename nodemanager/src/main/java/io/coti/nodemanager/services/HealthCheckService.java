package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.LockData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import io.coti.nodemanager.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthCheckService implements IHealthCheckService {

    private static final String NODE_HEALTH_END_POINT = "/health/state";
    private static final int RETRY_INTERVAL_IN_SECONDS = 20;
    private static final int MAX_NUM_OF_TRIES = 3;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private final Map<Hash, Thread> hashToThreadMap = new ConcurrentHashMap<>();
    private final LockData nodeHashLockData = new LockData();
    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private ActiveNodes activeNodes;
    private RestTemplate restTemplate;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    public void init() {
        nodesHealthCheck();
        initRestTemplate();
    }

    private void initRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        restTemplate = new RestTemplate(factory);
    }

    private void nodesHealthCheck() {
        try {
            checkNodesList(networkService.getNetworkNodeDataList());
        } catch (Exception e) {
            log.error("Exception in health check: ", e);
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
                BaseNodeMonitorService.HealthState reportedHealthState =
                        restTemplate.getForObject(networkNodeDataToCheck.getHttpFullAddress() + NODE_HEALTH_END_POINT, BaseNodeMonitorService.HealthState.class);

                if (reportedHealthState != null) {
                    log.debug("{} of address {} and port {} is responding to healthcheck {}.",
                            networkNodeDataToCheck.getNodeType(), networkNodeDataToCheck.getAddress(), networkNodeDataToCheck.getHttpPort(), reportedHealthState);
                    if (networkNodeDataToCheck.getReportedHealthState() != reportedHealthState) {
                        networkNodeDataToCheck.setReportedHealthState(reportedHealthState);
                        webSocketSender.notifyNodeHealthState(networkNodeDataToCheck.getNodeHash(), reportedHealthState);
                    }
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

    private NetworkNodeData checkAndDeleteNodeIfNeeded(NetworkNodeData networkNodeData) {
        NetworkNodeData nodeToRemove = null;
        if (!isNodeConnected(networkNodeData)) {
            deleteNodeRecord(networkNodeData);
            nodeToRemove = networkNodeData;
        }
        return nodeToRemove;
    }

    private void checkNodesList(List<NetworkNodeData> nodesList) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        try {
            nodesList.forEach(networkNodeData -> initNodeMonitorThreadIfAbsent(threadFactory, networkNodeData));
        } catch (Exception e) {
            log.error("Error while checking nodeList", e);
        }
    }

    public void initNodeMonitorThreadIfAbsent(ThreadFactory threadFactory, NetworkNodeData networkNodeData) {
        Hash nodeHash = networkNodeData.getNodeHash();
        try {
            synchronized (nodeHashLockData.addLockToLockMap(nodeHash)) {
                Runnable nodeMonitorTask = () -> monitorNode(networkNodeData);
                Thread thread = hashToThreadMap.get(nodeHash);
                if (thread != null) {
                    thread.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                thread = threadFactory.newThread(nodeMonitorTask);
                thread.setName(nodeHash.toString());
                hashToThreadMap.put(nodeHash, thread);
                thread.start();
            }

        } finally {
            nodeHashLockData.removeLockFromLocksMap(nodeHash);
        }
    }

    private void monitorNode(NetworkNodeData networkNodeData) {
        Hash nodeHash = networkNodeData.getNodeHash();
        boolean terminateThread = false;
        while (!Thread.currentThread().isInterrupted() && !terminateThread) {
            try {
                Thread.sleep(5000);
                NetworkNodeData networkNodeDataToRemove = checkAndDeleteNodeIfNeeded(networkNodeData);
                if (networkNodeDataToRemove != null) {
                    networkService.removeNode(networkNodeDataToRemove);
                    nodeManagementService.propagateNetworkChanges();
                    hashToThreadMap.remove(nodeHash);
                    terminateThread = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Exception in monitorNode: ", e);
            }
        }
    }

    private void deleteNodeRecord(NetworkNodeData networkNodeData) {
        log.info("Deleting {} of address {} and port {}", networkNodeData.getNodeType(), networkNodeData.getAddress(), networkNodeData.getHttpPort());
        nodeManagementService.addNodeHistory(networkNodeData, NetworkNodeStatus.INACTIVE, Instant.now());
        activeNodes.delete(networkNodeData);
    }

    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        try {
            hashToThreadMap.forEach((nodeHash, thread) -> {
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            log.error("Interrupted shutdown health check service");
        }
    }
}
