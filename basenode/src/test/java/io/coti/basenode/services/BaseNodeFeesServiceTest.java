package io.coti.basenode.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.http.ConstantTokenFeeSetRequest;
import io.coti.basenode.http.DeleteTokenFeeRequest;
import io.coti.basenode.http.GetNodeFeesDataResponse;
import io.coti.basenode.http.RatioTokenFeeSetRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.NodeFees;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeFees;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
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
    Map<Hash, TokenFeeData> nodeFeesMap = new ConcurrentHashMap<>();
    ConstantTokenFeeData defaultGenerationNodeConstantTokenFeeData;

    @BeforeEach
    void init() {
        BaseNodeServiceManager.nodeFees = nodeFeesLocal;
        BaseNodeServiceManager.databaseConnector = databaseConnector;
        BaseNodeServiceManager.networkService = networkService;
        NetworkNodeData networkNodeDataMock = new NetworkNodeData();
        when(networkService.getNetworkNodeData()).thenReturn(networkNodeDataMock);
        List<TokenFeeData> defaultTokenFeeDataList = new ArrayList<>();

        defaultGenerationNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_GENERATION_FEE, BigDecimal.ONE);
        ConstantTokenFeeData defaultMintingNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_MINTING_FEE, BigDecimal.ONE);
        defaultTokenFeeDataList.add(defaultGenerationNodeConstantTokenFeeData);
        defaultTokenFeeDataList.add(defaultMintingNodeConstantTokenFeeData);
        baseNodeFeesService.init(defaultTokenFeeDataList);
    }

    @Test
    void etc() {
        TokenFeeData feeData = new TokenFeeData() {
            @Override
            public BigDecimal getFeeAmount(BigDecimal amount) {
                return null;
            }

            @Override
            public boolean valid() {
                return false;
            }
        };
        feeData.valid();

        ConstantTokenFeeData constantTokenFeeData = new ConstantTokenFeeData();
        Assertions.assertEquals(false, constantTokenFeeData.valid());
    }

    @Test
    void ratio() {
        FeeData feeData1 = new FeeData(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN);
        RatioTokenFeeData ratioTokenFeeData = new RatioTokenFeeData("*", NodeFeeType.FULL_NODE_FEE, feeData1);
        ratioTokenFeeData.toString();
        Assertions.assertEquals(BigDecimal.ONE, ratioTokenFeeData.getFeeAmount(BigDecimal.valueOf(100)));
        FeeData feeData2 = new FeeData(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN);
        RatioTokenFeeData ratioTokenFeeData2 = new RatioTokenFeeData("*", NodeFeeType.FULL_NODE_FEE, feeData2);
        ratioTokenFeeData2.toString();
        BigDecimal bigDecimal100 = BigDecimal.valueOf(100);
        Assertions.assertThrows(CotiRunTimeException.class, () -> ratioTokenFeeData2.getFeeAmount(bigDecimal100));
    }

    @Test
    void getNodeFeeData() {
        Hash tokenHash = OriginatorCurrencyCrypto.calculateHash("*");
        Assertions.assertEquals(nodeFeesMap.get(NodeFeeType.TOKEN_MINTING_FEE.getHash()), baseNodeFeesService.getTokenFeeData(tokenHash, NodeFeeType.TOKEN_MINTING_FEE));
    }

    @Test
    void calculateClassicFee() {
        Hash anySymbol = OriginatorCurrencyCrypto.calculateHash("something");
        Hash defaultTokenHash = OriginatorCurrencyCrypto.calculateHash("*");
        ConstantTokenFeeData defaultGenerationNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_GENERATION_FEE, BigDecimal.ONE);
        when(baseNodeFeesService.getTokenFeeData(defaultTokenHash, NodeFeeType.TOKEN_GENERATION_FEE)).thenReturn(defaultGenerationNodeConstantTokenFeeData);
        Assertions.assertEquals(new BigDecimal(1), baseNodeFeesService.calculateClassicFee(anySymbol, NodeFeeType.TOKEN_GENERATION_FEE, new BigDecimal(1000000)));
        ConstantTokenFeeData defaultMintingNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_GENERATION_FEE, BigDecimal.TEN);
        when(baseNodeFeesService.getTokenFeeData(defaultTokenHash, NodeFeeType.TOKEN_MINTING_FEE)).thenReturn(defaultMintingNodeConstantTokenFeeData);
        Assertions.assertEquals(new BigDecimal(10), baseNodeFeesService.calculateClassicFee(anySymbol, NodeFeeType.TOKEN_MINTING_FEE, new BigDecimal(10)));
        ConstantTokenFeeData anySymbolFee = new ConstantTokenFeeData("something", NodeFeeType.FULL_NODE_FEE, BigDecimal.valueOf(12));
        when(baseNodeFeesService.getTokenFeeData(anySymbol, NodeFeeType.FULL_NODE_FEE)).thenReturn(anySymbolFee);
        Assertions.assertEquals(new BigDecimal(12), baseNodeFeesService.calculateClassicFee(anySymbol, NodeFeeType.FULL_NODE_FEE, new BigDecimal(555)));
    }

    @Test
    void setFeeValue_negative() {
        ConstantTokenFeeSetRequest constantTokenFeeSetRequest = new ConstantTokenFeeSetRequest();
        constantTokenFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        constantTokenFeeSetRequest.setConstant(BigDecimal.valueOf(-1));
        constantTokenFeeSetRequest.setTokenSymbol("*");
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(constantTokenFeeSetRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void setRatioFeeValue() {
        RatioTokenFeeSetRequest ratioTokenFeeSetRequest = new RatioTokenFeeSetRequest();
        ratioTokenFeeSetRequest.setTokenSymbol("*");
        ratioTokenFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        ratioTokenFeeSetRequest.setFeeMinimum(new BigDecimal(2));
        ratioTokenFeeSetRequest.setFeeMaximum(new BigDecimal(50));
        ratioTokenFeeSetRequest.setFeePercentage(new BigDecimal(20));
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(ratioTokenFeeSetRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(new BigDecimal(50), ((RatioTokenFeeData) ((GetNodeFeesDataResponse) response.getBody()).getNodeFeeDataArrayList().get(0)).getFeeData().getMaximumFee());
        Assertions.assertEquals(new BigDecimal(20), ((RatioTokenFeeData) ((GetNodeFeesDataResponse) response.getBody()).getNodeFeeDataArrayList().get(0)).getFeeData().getFeePercentage());
        Assertions.assertEquals(new BigDecimal(2), ((RatioTokenFeeData) ((GetNodeFeesDataResponse) response.getBody()).getNodeFeeDataArrayList().get(0)).getFeeData().getMinimumFee());
    }

    @Test
    void setConstantFeeValue() {
        ConstantTokenFeeSetRequest constantTokenFeeSetRequest = new ConstantTokenFeeSetRequest();
        constantTokenFeeSetRequest.setConstant(BigDecimal.valueOf(2));
        constantTokenFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        constantTokenFeeSetRequest.setTokenSymbol("*");
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(constantTokenFeeSetRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(new BigDecimal(2), ((ConstantTokenFeeData) ((GetNodeFeesDataResponse) response.getBody()).getNodeFeeDataArrayList().get(0)).getConstant());
    }

    @ParameterizedTest
    @MethodSource
    void setInvalidFeeValue(int minimum, int maximum, int percentage) {
        RatioTokenFeeSetRequest ratioTokenFeeSetRequest = new RatioTokenFeeSetRequest();
        ratioTokenFeeSetRequest.setTokenSymbol("*");
        ratioTokenFeeSetRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        ratioTokenFeeSetRequest.setFeeMinimum(new BigDecimal(minimum));
        ratioTokenFeeSetRequest.setFeeMaximum(new BigDecimal(maximum));
        ratioTokenFeeSetRequest.setFeePercentage(new BigDecimal(percentage));
        ResponseEntity<IResponse> response = baseNodeFeesService.setFeeValue(ratioTokenFeeSetRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private static Stream<Arguments> setInvalidFeeValue() {
        return Stream.of(arguments(10, 10, -1),
                arguments(10, 10, 0),
                arguments(20, 10, 20),
                arguments(10, 10, 101));
    }

    @Test
    void getNodeFees() {
        ResponseEntity<IResponse> response = baseNodeFeesService.getNodeFees();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteFee() {
        when(nodeFees.getByHash(any(Hash.class))).thenReturn(defaultGenerationNodeConstantTokenFeeData);
        DeleteTokenFeeRequest deleteTokenFeeRequest = new DeleteTokenFeeRequest();
        deleteTokenFeeRequest.setTokenSymbol("*");
        deleteTokenFeeRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        ResponseEntity<IResponse> response = baseNodeFeesService.deleteFeeValue(deleteTokenFeeRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void failedDeleteFee() {
        when(nodeFees.getByHash(any(Hash.class))).thenReturn(null);
        DeleteTokenFeeRequest deleteTokenFeeRequest = new DeleteTokenFeeRequest();
        deleteTokenFeeRequest.setTokenSymbol("null");
        deleteTokenFeeRequest.setNodeFeeType(NodeFeeType.TOKEN_GENERATION_FEE);
        ResponseEntity<IResponse> response = baseNodeFeesService.deleteFeeValue(deleteTokenFeeRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}