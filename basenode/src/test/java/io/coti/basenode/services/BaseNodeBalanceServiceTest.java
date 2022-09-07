package io.coti.basenode.services;

import io.coti.basenode.data.Event;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.GetTokenBalancesResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.coti.basenode.utils.HashTestUtils.generateRandomAddressHash;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeBalanceService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeBalanceServiceTest {

    @Autowired
    private BaseNodeBalanceService balanceService;
    @MockBean
    protected ICurrencyService currencyService;
    @MockBean
    protected IEventService nodeEventService;

    @Before
    public void init() {
        balanceService.init();
    }

    @Test
    public void getCurrencyBalances_noNative_valuesMatch() {
        when(nodeEventService.eventHappened(isA(Event.MULTI_DAG.getClass()))).thenReturn(true);
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));

        GetTokenBalancesRequest getCurrencyBalanceRequest = new GetTokenBalancesRequest();
        Hash tokenHash1 = generateRandomAddressHash();
        Hash tokenHash2 = generateRandomAddressHash();
        Hash addressHash1 = generateRandomAddressHash();
        Hash addressHash2 = generateRandomAddressHash();
        List<Hash> addresses = Arrays.asList(addressHash1, addressHash2);
        getCurrencyBalanceRequest.setAddresses(addresses);

        HashMap<Hash, BigDecimal> currencyHashToAmountMap1 = new HashMap<>();
        currencyHashToAmountMap1.put(tokenHash1, BigDecimal.TEN);
        currencyHashToAmountMap1.put(tokenHash2, BigDecimal.ONE);
        balanceService.balanceMap.put(addressHash1, currencyHashToAmountMap1);
        balanceService.balanceMap.put(addressHash2, currencyHashToAmountMap1);
        HashMap<Hash, BigDecimal> currencyHashToAmountMap2 = new HashMap<>();
        currencyHashToAmountMap2.put(tokenHash1, BigDecimal.ZERO);
        currencyHashToAmountMap2.put(tokenHash2, BigDecimal.ONE);
        balanceService.preBalanceMap.put(addressHash1, currencyHashToAmountMap2);
        balanceService.preBalanceMap.put(addressHash2, currencyHashToAmountMap2);
        ResponseEntity<IResponse> currencyBalances = balanceService.getTokenBalances(getCurrencyBalanceRequest);

        Assert.assertEquals(HttpStatus.OK, currencyBalances.getStatusCode());
        Assert.assertTrue(((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).containsKey(tokenHash1));
        Assert.assertTrue(((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).containsKey(tokenHash2));
        Assert.assertEquals(BigDecimal.TEN, ((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).get(tokenHash1).getAddressBalance());
        Assert.assertEquals(BigDecimal.ONE, ((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).get(tokenHash2).getAddressBalance());
    }

    @Test
    public void rollbackBaseTransactions() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Map<Hash, Map<Hash, BigDecimal>> preBalanceMap = new ConcurrentHashMap<>();
        AtomicReference<Hash> currencyHash = new AtomicReference<>();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            baseTransactionData.setAmount(new BigDecimal(-2));
            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            currencyHash.set(baseTransactionData.getCurrencyHash());
            preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
            preBalanceMap.get(addressHash).putIfAbsent(currencyHash.get(), amount);
            when(currencyService.getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash())).thenReturn(baseTransactionData.getCurrencyHash());
        });
        ReflectionTestUtils.setField(balanceService, "preBalanceMap", preBalanceMap);
        balanceService.rollbackBaseTransactions(transactionData);
        Assert.assertEquals(BigDecimal.ZERO, preBalanceMap.get(transactionData.getBaseTransactions().get(0).getAddressHash()).get(currencyHash.get()));
    }
}
