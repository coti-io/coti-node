package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeFeeData;
import io.coti.basenode.data.NodeFeeType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetNodeFeesDataResponse;
import io.coti.basenode.http.NodeFeeSetRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.NodeFees;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeFees;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeFeesService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeFeesServiceTest {

    @Autowired
    private BaseNodeFeesService baseNodeFeesService;
    @MockBean
    NodeFees nodeFeesLocal;
    @MockBean
    IDatabaseConnector databaseConnector;
    @MockBean
    INetworkService networkService;
    List<NodeFeeType> nodeFeeTypeList = new ArrayList<>();
    Map<Hash, NodeFeeData> nodeFeesMap = new ConcurrentHashMap<>();

    @BeforeEach
    void init() {
        nodeFees = nodeFeesLocal;
        nodeFeeTypeList.addAll(Arrays.asList(NodeFeeType.TOKEN_MINTING_FEE, NodeFeeType.TOKEN_GENERATION_FEE));
        when(nodeFees.getByHash(any(Hash.class))).then((a -> nodeFeesMap.get(a.getArgument(0))));
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            nodeFeesMap.put(((NodeFeeData) arg0).getNodeFeeType().getHash(), (NodeFeeData) arg0);
            return null;
        }).when(nodeFees).put(any(NodeFeeData.class));
        baseNodeFeesService.init(nodeFeeTypeList);
    }

    @Test
    void getNodeFeeData() {
        Assertions.assertEquals(nodeFeesMap.get(NodeFeeType.TOKEN_MINTING_FEE.getHash()), baseNodeFeesService.getNodeFeeData(NodeFeeType.TOKEN_MINTING_FEE));
    }

    @Test
    void calculateClassicFee() {
        Assertions.assertEquals(new BigDecimal(25), baseNodeFeesService.calculateClassicFee(NodeFeeType.TOKEN_GENERATION_FEE, new BigDecimal(1000000)));
        Assertions.assertEquals(new BigDecimal(1), baseNodeFeesService.calculateClassicFee(NodeFeeType.TOKEN_GENERATION_FEE, new BigDecimal(10)));
    }

    @Test
    void setFeeValue_negative() {
        NodeFeeSetRequest nodeFeeSetRequest = new NodeFeeSetRequest();
        nodeFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        nodeFeeSetRequest.setFeePercentage(new BigDecimal(120));
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(nodeFeeSetRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void setFeeValue() {
        NodeFeeSetRequest nodeFeeSetRequest = new NodeFeeSetRequest();
        nodeFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        nodeFeeSetRequest.setFeeMinimum(new BigDecimal(2));
        nodeFeeSetRequest.setFeeMaximum(new BigDecimal(50));
        nodeFeeSetRequest.setFeePercentage(new BigDecimal(20));
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(nodeFeeSetRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(new BigDecimal(50), ((GetNodeFeesDataResponse) response.getBody()).getNodeFeeDataArrayList().get(0).getFeeData().getMaximumFee());
    }

    @Test
    void getNodeFees() {
        ResponseEntity<IResponse> response = baseNodeFeesService.getNodeFees();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}