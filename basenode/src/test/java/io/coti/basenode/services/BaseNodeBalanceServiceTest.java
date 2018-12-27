package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.Arrays;

import static testUtils.TestUtils.*;

;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BaseNodeBalanceService.class)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        BaseNodeBalanceServiceTest.class})
@Slf4j
public class BaseNodeBalanceServiceTest extends AbstractTestExecutionListener {

    @Autowired
    private BaseNodeBalanceService baseNodeBalanceService;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        BaseNodeBalanceService baseNodeBalanceService =
                testContext.getApplicationContext().getBean("baseNodeBalanceService", BaseNodeBalanceService.class);
        baseNodeBalanceService.init();
    }

    @Test
    public void checkBalancesAndAddToPreBalance_isValid() {
        BaseTransactionData outputBaseTransactionData1 = generateReceiverBaseTransactionData(generateRandomHash(), generateRandomPositiveAmount());
        BaseTransactionData outputBaseTransactionData2 = generateReceiverBaseTransactionData(generateRandomHash(), generateRandomPositiveAmount());

        boolean result = baseNodeBalanceService.checkBalancesAndAddToPreBalance(new ArrayList<>(
                Arrays.asList(outputBaseTransactionData1, outputBaseTransactionData2)));

        Assert.assertTrue(result);
    }

    @Test
    public void checkBalancesAndAddToPreBalance_isNotValid() {
        BaseTransactionData inputBaseTransactionData1 = generateRandomInputBaseTransactionData(generateRandomHash(), generateRandomNegativeAmount());
        BaseTransactionData outputBaseTransactionData1 = generateReceiverBaseTransactionData(generateRandomHash(), generateRandomPositiveAmount());

        // inputBaseTransactionData1 address is negative. checkBalancesAndAddToPreBalance will return false
        boolean result = baseNodeBalanceService.checkBalancesAndAddToPreBalance(new ArrayList<>(
                Arrays.asList(inputBaseTransactionData1, outputBaseTransactionData1 )));

        Assert.assertFalse(result);
    }
}