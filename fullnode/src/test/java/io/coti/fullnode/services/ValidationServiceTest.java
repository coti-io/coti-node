package io.coti.fullnode.services;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeIdentityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import utils.TestUtils;

import static io.coti.fullnode.services.NodeServiceManager.feeService;

@ContextConfiguration(classes = {FeeService.class, ValidationService.class, BaseNodeIdentityService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ValidationServiceTest {

    @Autowired
    FeeService feeServiceLocal;
    @Autowired
    ValidationService validationService;
    @Autowired
    BaseNodeIdentityService nodeIdentityService;

    @BeforeEach
    void init() {
        feeService = feeServiceLocal;
    }

    @Test
    void validateFullNodeFeeDataIntegrity() {
        NodeServiceManager.nodeIdentityService = nodeIdentityService;
        TransactionData transactionData = TestUtils.createRandomTransaction();
        Hash addressHash = feeServiceLocal.getAddress();
        FullNodeFeeData fullNodeFeeData = TestUtils.generateFullNodeFeeData(addressHash, 10);
        transactionData.getBaseTransactions().add(fullNodeFeeData);
        Assertions.assertTrue(validationService.validateFullNodeFeeDataIntegrity(transactionData));
    }
}