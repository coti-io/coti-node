package io.coti.nodemanager.services;

import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.nodemanager.crypto.StakingNodeCrypto;
import io.coti.nodemanager.model.*;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import io.coti.nodemanager.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static IHealthCheckService healthCheckService;
    public static INetworkHistoryService networkHistoryService;
    public static INodeManagementService nodeManagementService;
    public static StakingService stakingService;
    public static StakingNodeCrypto stakingNodeCrypto;
    public static ActiveNodes activeNodes;
    public static NodeDailyActivities nodeDailyActivities;
    public static NodeHistory nodeHistory;
    public static ReservedHosts reservedHosts;
    public static StakingNodes stakingNodes;
    public static WebSocketSender webSocketSender;

    @Autowired
    public IHealthCheckService autowiredHealthCheckService;
    @Autowired
    public INetworkHistoryService autowiredNetworkHistoryService;
    @Autowired
    public INodeManagementService autowiredNodeManagementService;
    @Autowired
    public StakingService autowiredStakingService;
    @Autowired
    public StakingNodeCrypto autowiredStakingNodeCrypto;
    @Autowired
    public ActiveNodes autowiredActiveNodes;
    @Autowired
    public NodeDailyActivities autowiredNodeDailyActivities;
    @Autowired
    public NodeHistory autowiredNodeHistory;
    @Autowired
    public ReservedHosts autowiredReservedHosts;
    @Autowired
    public StakingNodes autowiredStakingNodes;
    @Autowired
    public WebSocketSender autowiredWebSocketSender;

}
