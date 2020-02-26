package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.*;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.database.RocksDBConnector;
import io.coti.nodemanager.http.AddNodePairEventRequest;
import io.coti.nodemanager.http.AddNodeSingleEventRequest;
import io.coti.nodemanager.http.GetNodeActivationTimeRequest;
import io.coti.nodemanager.http.GetNodeActivationTimeResponse;
import io.coti.nodemanager.model.*;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

//import io.coti.basenode.http.data.NodeTypeName;
//import io.coti.nodemanager.http.AddNodeBeginEventPairAdminRequest;
//import io.coti.nodemanager.http.AddNodeEventAdminRequest;
//import java.util.Random;


@Slf4j
@ContextConfiguration(classes = {NodeManagementService.class, RocksDBConnector.class, NodeHistory.class, NodeDailyActivities.class, InitializationService.class, NetworkHistoryService.class})
@TestPropertySource(locations = "classpath:properties")
@RunWith(SpringRunner.class)
@SpringBootTest()
public class NodeManagementServiceDoNotTakeItToDevTest {
    @Autowired
    private NodeManagementService nodeManagementService;
    @Autowired
    private NetworkHistoryService networkHistoryService;
    @MockBean
    private ReservedHosts reservedHosts;
    @Autowired
    private InitializationService initializationService;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private NodeDailyActivities nodeDailyActivities;
    @MockBean
    private ActiveNodes activeNodes;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private StakingService stakingService;
    @MockBean
    private StakingNodes stakingNodes;
    @MockBean
    private IAwsService awsService;
    @MockBean
    private IDBRecoveryService dbRecoveryService;
    @MockBean
    private IShutDownService shutDownService;
    @MockBean
    private IHealthCheckService healthCheckService;
    @MockBean
    private ICommunicationService communicationService;
    @MockBean
    private ApplicationContext applicationContext;
    @MockBean
    private BuildProperties buildProperties;


    private static final Hash fakeNode1 = new Hash("76d7f333480680c06df7d5c3cc17ffe2dc052597ae857285e57da249f4df344cf3e112739eca2aea63437f9e9819fac909ab93801b99853c779d8b6f5dcafb74");
    private static final Hash fakeNode2 = new Hash("a2d27c3248e3530c55ca0941fd0fe5f419efcb6f923e54fe83ec5024040f86d107c6882f6a2435408964c2e9f522579248c8a983a2761a03ba253e7ca7898e53");
    private static final Hash fakeNode3 = new Hash("0aa389aa3d8b31ecc5b2fa9164a0a2f52fb59165730de4527441b0278e5e47e51e3e1e69cf24a1a0bb58a53b262c185c4400f0d2f89b469c9498b6ed517b7398");
    private static final Hash fakeNode4 = new Hash("e70a7477209fa59b3e866b33184ae47e5bed0d202c7214a4a93fd2592b11c3b567f2e85d28f3fc415401bb5a6b8be9eae5e77aa18d7e042c33ba91396d3cd970");
    private static final Hash fakeNode5 = new Hash("5a4a7a8b72384bd6310135fdd939d1b105aec81a6ad72d745e5636770690a17c31eb6a775860b65b6211ec27d0690802032123a7f34f3acb68ed5d66366cd003");
    private static final Hash fakeNode6 = new Hash("cd10ad2f479647dab74c0017958399a9ce87a56672bfd36739c70c4ddd2b2b5f451ff5deb10c86b745fcfa08dcb3ff1f331124bca608f5eab247ad1ec6e18281");
    private static final Hash fakeNode7 = new Hash("b1fdc0efa64be6de413f888a3ed7aa4809d65c1a20b50ad775e9caeddaa1ae9c5027345157b1be789abd6961c410956be45da20de8444e874d8e487c6dfaa52b");

    @Test
    public void addNodeHistoryTest() {

        NetworkNodeStatus nodeStatus;
        Instant eventDateTime;

        NetworkNodeData networkNodeData1 = new NetworkNodeData();
        networkNodeData1.setHash(fakeNode1);
        networkNodeData1.setNodeType(NodeType.FullNode);
        networkNodeData1.setAddress("test");
        networkNodeData1.setHttpPort("000");
        networkNodeData1.setPropagationPort("000");
        networkNodeData1.setReceivingPort("000");
        networkNodeData1.setNetworkType(NetworkType.TestNet);
        networkNodeData1.setTrustScore(17.0);
        networkNodeData1.setWebServerUrl("test");
        networkNodeData1.setFeeData(new FeeData(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1)));
        networkNodeData1.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData1.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData1.getNodeRegistrationData().setNodeHash(fakeNode1);
        networkNodeData1.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData1.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData1.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData1.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData1.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        eventDateTime = LocalDateTime.of(2019, 11, 27, 10, 0, 0).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);
        eventDateTime = LocalDateTime.of(2019, 12, 2, 11, 5, 27).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);
        eventDateTime = LocalDateTime.of(2019, 12, 3, 15, 34, 32).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);
        eventDateTime = LocalDateTime.of(2019, 12, 11, 1, 52, 11).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 15, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);
        eventDateTime = LocalDateTime.of(2019, 12, 16, 7, 22, 22).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 25, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 30, 3, 28, 1).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData1, nodeStatus, eventDateTime);

        log.info("fakeNode1 finished");

        NetworkNodeData networkNodeData2 = new NetworkNodeData();
        networkNodeData2.setHash(fakeNode2);
        networkNodeData2.setNodeType(NodeType.FullNode);
        networkNodeData2.setAddress("test");
        networkNodeData2.setHttpPort("000");
        networkNodeData2.setPropagationPort("000");
        networkNodeData2.setReceivingPort("000");
        networkNodeData2.setNetworkType(NetworkType.TestNet);
        networkNodeData2.setTrustScore(77.0);
        networkNodeData2.setWebServerUrl("test");
        networkNodeData2.setFeeData(new FeeData(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2)));
        networkNodeData2.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData2.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData2.getNodeRegistrationData().setNodeHash(fakeNode2);
        networkNodeData2.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData2.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData2.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData2.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData2.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        eventDateTime = LocalDateTime.of(2019, 12, 1, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 5, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 10, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 10, 6, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 10, 10, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 15, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 20, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 25, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData2, nodeStatus, eventDateTime);

        log.info("fakeNode2 finished");

        NetworkNodeData networkNodeData3 = new NetworkNodeData();
        networkNodeData3.setHash(fakeNode3);
        networkNodeData3.setNodeType(NodeType.FullNode);
        networkNodeData3.setAddress("test");
        networkNodeData3.setHttpPort("000");
        networkNodeData3.setPropagationPort("000");
        networkNodeData3.setReceivingPort("000");
        networkNodeData3.setNetworkType(NetworkType.TestNet);
        networkNodeData3.setTrustScore(37.0);
        networkNodeData3.setWebServerUrl("test");
        networkNodeData3.setFeeData(new FeeData(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.3)));
        networkNodeData3.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData3.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData3.getNodeRegistrationData().setNodeHash(fakeNode3);
        networkNodeData3.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData3.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData3.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData3.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData3.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        LocalDateTime startDateTime = LocalDateTime.of(2019, 11, 25, 0, 0, 0);
        Instant localDateTime;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 1; k++) {  // or 60
                    nodeStatus = NetworkNodeStatus.ACTIVE;
                    localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), j, k, 0).toInstant(ZoneOffset.UTC);
                    nodeManagementService.addNodeHistory(networkNodeData3, nodeStatus, localDateTime);

                    nodeStatus = NetworkNodeStatus.INACTIVE;
                    localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), j, k, 30).toInstant(ZoneOffset.UTC);
                    nodeManagementService.addNodeHistory(networkNodeData3, nodeStatus, localDateTime);
                }

            }
            log.info("fakeNode3 finished" + startDateTime);
            startDateTime = startDateTime.plusDays(1);
        }

        NetworkNodeData networkNodeData4 = new NetworkNodeData();
        networkNodeData4.setHash(fakeNode4);
        networkNodeData4.setNodeType(NodeType.FullNode);
        networkNodeData4.setAddress("test");
        networkNodeData4.setHttpPort("000");
        networkNodeData4.setPropagationPort("000");
        networkNodeData4.setReceivingPort("000");
        networkNodeData4.setNetworkType(NetworkType.TestNet);
        networkNodeData4.setTrustScore(77.0);
        networkNodeData4.setWebServerUrl("test");
        networkNodeData4.setFeeData(new FeeData(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.2)));
        networkNodeData4.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData4.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData4.getNodeRegistrationData().setNodeHash(fakeNode4);
        networkNodeData4.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData4.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData4.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData4.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData4.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        eventDateTime = LocalDateTime.of(2019, 11, 15, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 5, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 10, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 15, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 20, 4, 41, 33).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2019, 12, 25, 18, 16, 3).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData4, nodeStatus, eventDateTime);

        log.info("fakeNode4 finished");

        NetworkNodeData networkNodeData5 = new NetworkNodeData();
        networkNodeData5.setHash(fakeNode5);
        networkNodeData5.setNodeType(NodeType.FullNode);
        networkNodeData5.setAddress("test");
        networkNodeData5.setHttpPort("000");
        networkNodeData5.setPropagationPort("000");
        networkNodeData5.setReceivingPort("000");
        networkNodeData5.setNetworkType(NetworkType.TestNet);
        networkNodeData5.setTrustScore(17.0);
        networkNodeData5.setWebServerUrl("test");
        networkNodeData5.setFeeData(new FeeData(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1)));
        networkNodeData5.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData5.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData5.getNodeRegistrationData().setNodeHash(fakeNode5);
        networkNodeData5.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData5.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData5.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData5.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData5.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        eventDateTime = LocalDateTime.of(2019, 11, 27, 10, 0, 0).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData5, nodeStatus, eventDateTime);

        log.info("fakeNode5 finished");

        NetworkNodeData networkNodeData6 = new NetworkNodeData();
        networkNodeData6.setHash(fakeNode6);
        networkNodeData6.setNodeType(NodeType.FullNode);
        networkNodeData6.setAddress("test");
        networkNodeData6.setHttpPort("000");
        networkNodeData6.setPropagationPort("000");
        networkNodeData6.setReceivingPort("000");
        networkNodeData6.setNetworkType(NetworkType.TestNet);
        networkNodeData6.setTrustScore(17.0);
        networkNodeData6.setWebServerUrl("test");
        networkNodeData6.setFeeData(new FeeData(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.1)));
        networkNodeData6.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData6.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData6.getNodeRegistrationData().setNodeHash(fakeNode6);
        networkNodeData6.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData6.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData6.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData6.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData6.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        eventDateTime = LocalDateTime.of(2019, 11, 27, 10, 0, 0).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.ACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData6, nodeStatus, eventDateTime);

        eventDateTime = LocalDateTime.of(2020, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC);
        nodeStatus = NetworkNodeStatus.INACTIVE;
        nodeManagementService.addNodeHistory(networkNodeData6, nodeStatus, eventDateTime);

        log.info("fakeNode6 finished");

        NetworkNodeData networkNodeData7 = new NetworkNodeData();
        networkNodeData7.setHash(fakeNode7);
        networkNodeData7.setNodeType(NodeType.FullNode);
        networkNodeData7.setAddress("test");
        networkNodeData7.setHttpPort("000");
        networkNodeData7.setPropagationPort("000");
        networkNodeData7.setReceivingPort("000");
        networkNodeData7.setNetworkType(NetworkType.TestNet);
        networkNodeData7.setTrustScore(37.0);
        networkNodeData7.setWebServerUrl("test");
        networkNodeData7.setFeeData(new FeeData(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7)));
        networkNodeData7.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData7.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData7.getNodeRegistrationData().setNodeHash(fakeNode7);
        networkNodeData7.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData7.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData7.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData7.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData7.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        startDateTime = LocalDateTime.of(2011, 1, 1, 0, 0, 0);

        for (int i = 0; i < 2555; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 2; k++) {  // or 60
                    nodeStatus = NetworkNodeStatus.ACTIVE;
                    localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), j, k, 0).toInstant(ZoneOffset.UTC);
                    nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

                    int remainder = i % 100;
                    if (remainder == 0 && j == 1 && k == 1) {
                        nodeStatus = NetworkNodeStatus.INACTIVE;
                        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), j, k, 30).toInstant(ZoneOffset.UTC);
                        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);
                    }
                }
            }
            startDateTime = startDateTime.plusDays(1);
        }
        log.info("fakeNode7 finished");
    }

    @Test
    public void addNodeEventAdminTest() {

        Instant startDateTime = LocalDateTime.of(2019, 10, 01, 10, 0, 0).toInstant(ZoneOffset.UTC);
        final Hash fakeNode8 = new Hash("6d27574a68615aca022d628046d3c01b3e370969cb9ed54ada1234f684e849a9bdc6a0ecc83872aa4b27ef2c15fb086b1529fc61c5a98b1d0438053cdb2679bc");
        Random rand = new Random();

        for (int i = 0; i < 6; i++) {
            int timeShift = rand.nextInt(1000) + 1;
            Instant eventInstant = startDateTime.plusSeconds(timeShift);

            NetworkNodeStatus nodeStatus;
            if (rand.nextInt(2) == 0) {
                nodeStatus = NetworkNodeStatus.INACTIVE;
            } else {
                nodeStatus = NetworkNodeStatus.ACTIVE;
            }

            AddNodeSingleEventRequest addNodeEventAdminRequest = new AddNodeSingleEventRequest(fakeNode8, eventInstant, NodeType.FullNode, nodeStatus);
            nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);
        }

        log.info("fakeNode8 finished");
    }


    @Test
    public void addNodeHistoryTest_rocksDBChecks() {
        Instant preStart = Instant.now();
        log.info("Test about to start {}", preStart);

        NetworkNodeData networkNodeData7 = new NetworkNodeData();
        networkNodeData7.setHash(fakeNode7);
        networkNodeData7.setNodeType(NodeType.FullNode);
        networkNodeData7.setAddress("test");
        networkNodeData7.setHttpPort("000");
        networkNodeData7.setPropagationPort("000");
        networkNodeData7.setReceivingPort("000");
        networkNodeData7.setNetworkType(NetworkType.TestNet);
        networkNodeData7.setTrustScore(37.0);
        networkNodeData7.setWebServerUrl("test");
        networkNodeData7.setFeeData(new FeeData(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7)));
        networkNodeData7.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData7.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData7.getNodeRegistrationData().setNodeHash(fakeNode7);
        networkNodeData7.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData7.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData7.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData7.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData7.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        Instant startDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);;

        NetworkNodeStatus nodeStatus = NetworkNodeStatus.ACTIVE;
        Random rand = new Random();

        for (int i = 0; i < 60000; i++) {
            int timeShift = rand.nextInt(1000) + 1 + i * 100;
            Instant eventInstant = startDateTime.plusSeconds(timeShift);

            nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, eventInstant);
        }

        log.info("fakeNode7 finished");
        Instant postEnd = Instant.now();
        log.info("Test ended {} after {} ms", postEnd, postEnd.toEpochMilli()-preStart.toEpochMilli());
    }

    @Test
    public void timeRocksDBCheck() {
        Instant preStart = Instant.now();
        log.info("Test about to start {}", preStart);
        NetworkNodeData networkNodeData7 = new NetworkNodeData();
        Instant postEnd = Instant.now();
        log.info("Test ended {} after {} ms", postEnd, postEnd.toEpochMilli()-preStart.toEpochMilli());
    }


    @Test
    public void addNodeEvent_failValidation_failedToAdd() {
        NetworkNodeData networkNodeData7 = new NetworkNodeData();
        networkNodeData7.setHash(fakeNode7);
        networkNodeData7.setNodeType(NodeType.FullNode);
        networkNodeData7.setAddress("test");
        networkNodeData7.setHttpPort("000");
        networkNodeData7.setPropagationPort("000");
        networkNodeData7.setReceivingPort("000");
        networkNodeData7.setNetworkType(NetworkType.TestNet);
        networkNodeData7.setTrustScore(37.0);
        networkNodeData7.setWebServerUrl("test");
        networkNodeData7.setFeeData(new FeeData(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7)));
        networkNodeData7.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData7.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData7.getNodeRegistrationData().setNodeHash(fakeNode7);
        networkNodeData7.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData7.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData7.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData7.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData7.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        LocalDateTime startDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        NetworkNodeStatus nodeStatus;
        Instant localDateTime;
        int iPlace;

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 1;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.INACTIVE;
        iPlace = 2;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 3;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 4;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 5;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth() + 2, iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        Instant newEventDateTime = LocalDateTime.of(2020, 1, startDateTime.getDayOfMonth(), 3, 7, 0).toInstant(ZoneOffset.UTC);
        NetworkNodeStatus eventNodeStatus = NetworkNodeStatus.ACTIVE;

        AddNodeSingleEventRequest addNodeEventAdminRequest = new AddNodeSingleEventRequest(fakeNode7, newEventDateTime, NodeType.FullNode, eventNodeStatus);
        ResponseEntity<IResponse> responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        addNodeEventAdminRequest.setNodeStatus(NetworkNodeStatus.INACTIVE);
        responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        newEventDateTime = LocalDateTime.of(2020, 2, startDateTime.getDayOfMonth(), 3, 7, 0).toInstant(ZoneOffset.UTC);
        addNodeEventAdminRequest.setRecordTime(newEventDateTime);
        addNodeEventAdminRequest.setNodeStatus(NetworkNodeStatus.ACTIVE);

        responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        newEventDateTime = LocalDateTime.of(2020, 1, startDateTime.getDayOfMonth() + 1, 3, 7, 0).toInstant(ZoneOffset.UTC);
        addNodeEventAdminRequest.setRecordTime(newEventDateTime);
        addNodeEventAdminRequest.setNodeStatus(NetworkNodeStatus.ACTIVE);

        responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        newEventDateTime = LocalDateTime.of(2020, 1, startDateTime.getDayOfMonth() + 2, 3, 7, 0).toInstant(ZoneOffset.UTC);
        addNodeEventAdminRequest.setRecordTime(newEventDateTime);
        addNodeEventAdminRequest.setNodeStatus(NetworkNodeStatus.ACTIVE);

        responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));

    }

    @Test
    public void getActivationTimes() {
        NetworkNodeData networkNodeData7 = new NetworkNodeData();
        networkNodeData7.setHash(fakeNode7);
        networkNodeData7.setNodeType(NodeType.FullNode);
        networkNodeData7.setAddress("test");
        networkNodeData7.setHttpPort("000");
        networkNodeData7.setPropagationPort("000");
        networkNodeData7.setReceivingPort("000");
        networkNodeData7.setNetworkType(NetworkType.TestNet);
        networkNodeData7.setTrustScore(37.0);
        networkNodeData7.setWebServerUrl("test");
        networkNodeData7.setFeeData(new FeeData(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.7)));
        networkNodeData7.setNodeSignature(new SignatureData("test", "test"));
        networkNodeData7.setNodeRegistrationData(new NodeRegistrationData());

        networkNodeData7.getNodeRegistrationData().setNodeHash(fakeNode7);
        networkNodeData7.getNodeRegistrationData().setNodeType(NodeType.FullNode.toString());
        networkNodeData7.getNodeRegistrationData().setNetworkType(NetworkType.TestNet.toString());
        networkNodeData7.getNodeRegistrationData().setCreationTime(Instant.now());
        networkNodeData7.getNodeRegistrationData().setRegistrarHash(new Hash("00"));
        networkNodeData7.getNodeRegistrationData().setRegistrarSignature(new SignatureData("test", "test"));

        LocalDateTime startDateTime = LocalDateTime.of(2020, 1, 5, 0, 0, 0);

        NetworkNodeStatus nodeStatus;
        Instant localDateTime;
        int iPlace;

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 1;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.INACTIVE;
        iPlace = 2;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 3;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.INACTIVE;
        iPlace = 4;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth(), iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 5;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth() + 2, iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);

        nodeStatus = NetworkNodeStatus.ACTIVE;
        iPlace = 6;
        localDateTime = LocalDateTime.of(startDateTime.getYear(), startDateTime.getMonth(), startDateTime.getDayOfMonth() + 2, iPlace, iPlace, 0).toInstant(ZoneOffset.UTC);
        nodeManagementService.addNodeHistory(networkNodeData7, nodeStatus, localDateTime);


        GetNodeActivationTimeRequest getNodeActivationTimeRequest = new GetNodeActivationTimeRequest();
        getNodeActivationTimeRequest.setNodeHash(fakeNode7);
        ResponseEntity<IResponse> responseResponseEntity;
        responseResponseEntity = networkHistoryService.getNodeActivationTime(getNodeActivationTimeRequest);

        Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.OK));
        GetNodeActivationTimeResponse getNodeActivationTimeResponse = (GetNodeActivationTimeResponse) responseResponseEntity.getBody();


        // For pair at start
//        Instant newEventDateTime = LocalDateTime.of(2020, 1, 1, 0, 7, 0).toInstant(ZoneOffset.UTC);
//        Instant endEventDateTime = LocalDateTime.of(2020, 1, 3, 0, 8, 0).toInstant(ZoneOffset.UTC);

//        Instant newEventDateTime = LocalDateTime.of(2020, 1, 6, 0, 7, 0).toInstant(ZoneOffset.UTC);
//        Instant endEventDateTime = LocalDateTime.of(2020, 1, 6, 0, 8, 0).toInstant(ZoneOffset.UTC);
        // For single active after inactive
        Instant newEventDateTime = LocalDateTime.of(2020, 1, 5, 2, 7, 0).toInstant(ZoneOffset.UTC);
        // For Active at the end
//        Instant newEventDateTime = LocalDateTime.of(2020, 1, startDateTime.getDayOfMonth() + 3, 0, 7, 0).toInstant(ZoneOffset.UTC);
        NetworkNodeStatus eventNodeStatus = NetworkNodeStatus.INACTIVE;

        boolean addSingle = true;
        if (addSingle) {
            AddNodeSingleEventRequest addNodeEventAdminRequest = new AddNodeSingleEventRequest(fakeNode7, newEventDateTime, NodeType.FullNode, eventNodeStatus);
            responseResponseEntity = nodeManagementService.addSingleNodeEvent(addNodeEventAdminRequest);

            Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.OK));

            ResponseEntity<IResponse> responseResponseEntity2 = networkHistoryService.getNodeActivationTime(getNodeActivationTimeRequest);

            Assert.assertTrue(responseResponseEntity2.getStatusCode().equals(HttpStatus.OK));
//            GetNodeActivationTimeResponse getNodeActivationTimeResponse2 = (GetNodeActivationTimeResponse) responseResponseEntity2.getBody();
//
//            Assert.assertTrue(getNodeActivationTimeResponse2.getActivationTime().isBefore(getNodeActivationTimeResponse2.getOriginalActivationTime()));
        } else {
            AddNodePairEventRequest addNodeBeginPairEventRequest = new AddNodePairEventRequest();
            addNodeBeginPairEventRequest.setNodeHash(fakeNode7);
            addNodeBeginPairEventRequest.setStartTime(newEventDateTime);
            addNodeBeginPairEventRequest.setNodeType(NodeType.FullNode);
            addNodeBeginPairEventRequest.setFirstEventNodeStatus(NetworkNodeStatus.ACTIVE);

//            addNodeBeginPairEventRequest.setEndTime(endEventDateTime);

            responseResponseEntity = nodeManagementService.addPairNodeEvent(addNodeBeginPairEventRequest);
            Assert.assertTrue(responseResponseEntity.getStatusCode().equals(HttpStatus.OK));

            ResponseEntity<IResponse> responseResponseEntity2 = networkHistoryService.getNodeActivationTime(getNodeActivationTimeRequest);

            Assert.assertTrue(responseResponseEntity2.getStatusCode().equals(HttpStatus.OK));
            GetNodeActivationTimeResponse getNodeActivationTimeResponse2 = (GetNodeActivationTimeResponse) responseResponseEntity2.getBody();

            Assert.assertTrue(getNodeActivationTimeResponse2.getActivationTime().isBefore(getNodeActivationTimeResponse2.getOriginalActivationTime()));
        }


    }

    @Test
    public void getNodesPercentages() {


    }

}