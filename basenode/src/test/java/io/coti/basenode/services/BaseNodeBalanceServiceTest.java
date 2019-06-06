package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import testUtils.BaseNodeTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static testUtils.BaseNodeTestUtils.*;


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
        BaseTransactionData outputBaseTxData1 = generateRandomReceiverBaseTxData();
        BaseTransactionData outputBaseTxData2 = generateRandomReceiverBaseTxData();

        boolean result = baseNodeBalanceService.checkBalancesAndAddToPreBalance(new ArrayList<>(
                Arrays.asList(outputBaseTxData1, outputBaseTxData2)));
        Assert.assertTrue(result);
    }


    @Test
    public void checkBalancesAndAddToPreBalance_isNotValid() {
        BaseTransactionData inputBaseTransactionData1 = generateRandomInputBaseTransactionData(generateRandomHash(), generateRandomNegativeAmount());
        BaseTransactionData outputBaseTransactionData1 = generateRandomReceiverBaseTxData();

        // InputBaseTransactionData1 address is negative. checkBalancesAndAddToPreBalance will return false
        boolean result = baseNodeBalanceService.checkBalancesAndAddToPreBalance(new ArrayList<>(
                Arrays.asList(inputBaseTransactionData1, outputBaseTransactionData1)));
        Assert.assertFalse(result);
    }


    @Test
    public void getBalances() {

        GetBalancesRequest getBalanceRequest = BaseNodeTestUtils.generateRandomBalanceRequest();
        ResponseEntity<GetBalancesResponse> balancesResponseResponseEntity = baseNodeBalanceService.getBalances(getBalanceRequest);

        Assert.assertTrue(HttpStatus.OK.equals(balancesResponseResponseEntity.getStatusCode()));
        Assert.assertTrue(balancesResponseResponseEntity.getBody().getStatus().equals("Success"));
        Assert.assertTrue(balancesResponseResponseEntity.getBody().getAddressesBalance().keySet().containsAll(
                getBalanceRequest.getAddresses().stream().map((Function<Hash, Object>) Hash::toString).collect(Collectors.toList())));
    }

    @Test
    public void rollbackBaseTransactions_validInput_success() {
        TransactionData txData = BaseNodeTestUtils.generateRandomTxData();
        BigDecimal bdValChange = BigDecimal.valueOf(-700);
        txData.getBaseTransactions().get(0).setAmount(bdValChange);
        BigDecimal bdValInitial = BigDecimal.valueOf(100);
        baseNodeBalanceService.preBalanceMap.put(txData.getBaseTransactions().get(0).getAddressHash(), bdValInitial);
        baseNodeBalanceService.rollbackBaseTransactions(txData);
        BigDecimal bigDecimalUpdated = baseNodeBalanceService.preBalanceMap.get(txData.getBaseTransactions().get(0).getAddressHash());

        Assert.assertEquals(bigDecimalUpdated, bdValInitial.subtract(bdValChange) );
    }

    @Test(expected = IllegalStateException.class)
    public void rollbackBaseTransactions_invalidInput_ThrowsException() {
        TransactionData txData = BaseNodeTestUtils.generateRandomTxData();
        BigDecimal bdValChange = BigDecimal.valueOf(700);
        txData.getBaseTransactions().get(0).setAmount(bdValChange);
        BigDecimal bdValInitial = BigDecimal.valueOf(100);
        baseNodeBalanceService.preBalanceMap.put(txData.getBaseTransactions().get(0).getAddressHash(), bdValInitial);
        baseNodeBalanceService.rollbackBaseTransactions(txData);
    }

    @Test
    public void validateBalances_validBalanceMaps_noException() {
        baseNodeBalanceService.preBalanceMap.put(generateRandomHash(), BigDecimal.valueOf(generateRandomPositiveAmount()));
        baseNodeBalanceService.balanceMap.put(generateRandomHash(), BigDecimal.valueOf(generateRandomPositiveAmount()));
        baseNodeBalanceService.validateBalances();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBalances_invalidPreBalanceMap_ExceptionThrown() {
        baseNodeBalanceService.preBalanceMap.put(generateRandomHash(), (BigDecimal.valueOf(generateRandomPositiveAmount())).negate());
        baseNodeBalanceService.balanceMap.put(generateRandomHash(), BigDecimal.valueOf(generateRandomPositiveAmount()));
        baseNodeBalanceService.validateBalances();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateBalances_invalidBalanceMap_ExceptionThrown() {
        baseNodeBalanceService.preBalanceMap.put(generateRandomHash(), BigDecimal.valueOf(generateRandomPositiveAmount()));
        baseNodeBalanceService.balanceMap.put(generateRandomHash(), (BigDecimal.valueOf(generateRandomPositiveAmount())).negate());
        baseNodeBalanceService.validateBalances();
    }

    @Test
    public void updateBalance() {
        BigDecimal bdVal = BigDecimal.valueOf(generateRandomPositiveAmount());
        Hash hash = generateRandomHash();
        Assert.assertNull(baseNodeBalanceService.balanceMap.get(hash));
        baseNodeBalanceService.updateBalance(hash, bdVal);
        Assert.assertNotNull(baseNodeBalanceService.balanceMap.get(hash));
        Assert.assertEquals(baseNodeBalanceService.balanceMap.get(hash), bdVal);
    }

    @Test
    public void updatePreBalance() {
        BigDecimal bdVal = BigDecimal.valueOf(generateRandomPositiveAmount());
        Hash hash = generateRandomHash();
        Assert.assertNull(baseNodeBalanceService.preBalanceMap.get(hash));
        baseNodeBalanceService.updatePreBalance(hash, bdVal);
        Assert.assertNotNull(baseNodeBalanceService.preBalanceMap.get(hash));
        Assert.assertEquals(baseNodeBalanceService.preBalanceMap.get(hash), bdVal);
    }


}