package io.coti.basenode.services;

import io.coti.basenode.data.Event;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InputBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.AddressBalanceData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.MULTI_DAG_IS_NOT_SUPPORTED;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.BaseNodeServiceManager.currencyService;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeEventService;
import static io.coti.basenode.utils.HashTestUtils.generateRandomAddressHash;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeBalanceService.class})

@ComponentScan(basePackages = {"services"})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeBalanceServiceTest {

    @Autowired
    BaseNodeBalanceService baseNodeBalanceService;
    @MockBean
    IEventService nodeEventServiceLocal;
    @MockBean
    ICurrencyService currencyServiceLocal;

    @BeforeEach
    void init() {
        nodeEventService = nodeEventServiceLocal;
        currencyService = currencyServiceLocal;
        baseNodeBalanceService.init();
    }

    @Test
    void checkBalancesAndAddToPreBalance_validBalanceChanges_checksPassed() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            if (baseTransactionData instanceof InputBaseTransactionData) {
                BigDecimal amount = baseTransactionData.getAmount();
                Hash addressHash = baseTransactionData.getAddressHash();
                baseNodeBalanceService.balanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
                baseNodeBalanceService.balanceMap.get(addressHash).putIfAbsent(baseTransactionData.getCurrencyHash(), amount.negate());
                baseNodeBalanceService.preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
                baseNodeBalanceService.preBalanceMap.get(addressHash).putIfAbsent(baseTransactionData.getCurrencyHash(), amount.negate());
            }
        });

        boolean balancesUpdated = baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions());

        assertTrue(balancesUpdated);
    }

    @Test
    void checkBalancesAndAddToPreBalance_invalidBalanceChange_checkFailed() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));

        boolean balancesUpdated = baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions());

        assertFalse(balancesUpdated);
    }

    @Test
    void checkBalancesAndAddToPreBalance_invalidPreBalanceChange_checkFailed() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));
        transactionData.getBaseTransactions().get(0).setAmount(BigDecimal.valueOf(-1));
        Hash currencyHash = transactionData.getBaseTransactions().get(0).getCurrencyHash();
        Hash addressHash = transactionData.getBaseTransactions().get(0).getAddressHash();
        baseNodeBalanceService.updateBalance(addressHash, currencyHash, BigDecimal.ONE);

        boolean balancesUpdated = baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions());

        assertFalse(balancesUpdated);
    }

    @Test
    void checkBalancesAndAddToPreBalance_validChange_preBalancesUpdated() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));
        transactionData.getBaseTransactions().get(0).setAmount(BigDecimal.valueOf(-1));
        transactionData.getBaseTransactions().get(1).setAmount(BigDecimal.valueOf(1));
        Hash currencyHash = transactionData.getBaseTransactions().get(0).getCurrencyHash();
        Hash addressHash = transactionData.getBaseTransactions().get(0).getAddressHash();
        baseNodeBalanceService.updateBalance(addressHash, currencyHash, BigDecimal.ONE);
        baseNodeBalanceService.updatePreBalance(addressHash, currencyHash, BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getBalance(addressHash, currencyHash));
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getPreBalance(addressHash, currencyHash));

        boolean balancesUpdated = baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions());

        assertTrue(balancesUpdated);
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getBalance(addressHash, currencyHash));
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getPreBalance(addressHash, currencyHash));
    }

    @Test
    void getBalances_singleAddress_valuesMatch() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Hash currencyHash = transactionData.getBaseTransactions().get(0).getCurrencyHash();
        Hash addressHash = transactionData.getBaseTransactions().get(0).getAddressHash();
        when(currencyService.getNativeCurrencyHashIfNull(any(Hash.class))).then(a -> a.getArgument(0));
        when(currencyService.getNativeCurrencyHash()).thenReturn(currencyHash);
        transactionData.getBaseTransactions().get(0).setAmount(BigDecimal.valueOf(-1));
        transactionData.getBaseTransactions().get(1).setAmount(BigDecimal.valueOf(1));
        baseNodeBalanceService.updateBalance(addressHash, currencyHash, BigDecimal.ONE);
        baseNodeBalanceService.updatePreBalance(addressHash, currencyHash, BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getBalance(addressHash, currencyHash));
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getPreBalance(addressHash, currencyHash));

        boolean balancesUpdated = baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions());

        assertTrue(balancesUpdated);
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getBalance(addressHash, currencyHash));
        assertEquals(BigDecimal.ONE, baseNodeBalanceService.getPreBalance(addressHash, currencyHash));

        GetBalancesRequest getBalancesRequest = new GetBalancesRequest();
        List<Hash> hashes = new ArrayList<>();
        hashes.add(addressHash);
        getBalancesRequest.setAddresses(hashes);
        ResponseEntity<GetBalancesResponse> getBalancesResponse = baseNodeBalanceService.getBalances(getBalancesRequest);
        Map<String, AddressBalanceData> addressesBalance = getBalancesResponse.getBody().getAddressesBalance();
        AddressBalanceData addressBalance = addressesBalance.get(addressHash.toHexString());
        assertEquals(baseNodeBalanceService.getBalance(addressHash, currencyHash), addressBalance.getAddressBalance());
        assertEquals(baseNodeBalanceService.getPreBalance(addressHash, currencyHash), addressBalance.getAddressPreBalance());
        assertEquals(HttpStatus.OK, getBalancesResponse.getStatusCode());
    }

    @Test
    void getTokenBalances_noMultiDAGEvent_notSupported() {
        when(nodeEventService.eventHappened(isA(Event.MULTI_DAG.getClass()))).thenReturn(false);
        GetTokenBalancesRequest getCurrencyBalanceRequest = new GetTokenBalancesRequest();

        ResponseEntity<IResponse> currencyBalances = baseNodeBalanceService.getTokenBalances(getCurrencyBalanceRequest);

        assertEquals(HttpStatus.BAD_REQUEST, currencyBalances.getStatusCode());
        assertEquals(MULTI_DAG_IS_NOT_SUPPORTED, ((Response) currencyBalances.getBody()).getMessage());
        assertEquals(STATUS_ERROR, ((Response) currencyBalances.getBody()).getStatus());
    }

    @Test
    void getCurrencyBalances_noNative_valuesMatch() {
        when(nodeEventServiceLocal.eventHappened(isA(Event.MULTI_DAG.getClass()))).thenReturn(true);
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
        baseNodeBalanceService.balanceMap.put(addressHash1, currencyHashToAmountMap1);
        baseNodeBalanceService.balanceMap.put(addressHash2, currencyHashToAmountMap1);
        HashMap<Hash, BigDecimal> currencyHashToAmountMap2 = new HashMap<>();
        currencyHashToAmountMap2.put(tokenHash1, BigDecimal.ZERO);
        currencyHashToAmountMap2.put(tokenHash2, BigDecimal.ONE);
        baseNodeBalanceService.preBalanceMap.put(addressHash1, currencyHashToAmountMap2);
        baseNodeBalanceService.preBalanceMap.put(addressHash2, currencyHashToAmountMap2);
        ResponseEntity<IResponse> currencyBalances = baseNodeBalanceService.getTokenBalances(getCurrencyBalanceRequest);

        assertEquals(HttpStatus.OK, currencyBalances.getStatusCode());
        assertTrue(((GetTokenBalancesResponse) Objects.requireNonNull(currencyBalances.getBody())).getTokenBalances().get(addressHash1).containsKey(tokenHash1));
        assertTrue(((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).containsKey(tokenHash2));
        assertEquals(BigDecimal.TEN, ((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).get(tokenHash1).getAddressBalance());
        assertEquals(BigDecimal.ONE, ((GetTokenBalancesResponse) currencyBalances.getBody()).getTokenBalances().get(addressHash1).get(tokenHash2).getAddressBalance());
    }

    @Test
    void rollbackBaseTransactions() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Map<Hash, Map<Hash, BigDecimal>> preBalanceMap = new ConcurrentHashMap<>();
        AtomicReference<Hash> currencyHash = new AtomicReference<>();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            currencyHash.set(baseTransactionData.getCurrencyHash());
            preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
            preBalanceMap.get(addressHash).merge(currencyHash.get(), amount, (a, b) -> b.add(a));
            when(currencyServiceLocal.getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash())).thenReturn(baseTransactionData.getCurrencyHash());
        });
        ReflectionTestUtils.setField(baseNodeBalanceService, "preBalanceMap", preBalanceMap);
        baseNodeBalanceService.rollbackBaseTransactions(transactionData);
        assertEquals(BigDecimal.ZERO, preBalanceMap.get(transactionData.getBaseTransactions().get(0).getAddressHash()).get(currencyHash.get()));
    }

    @Test
    void checkBalancesAndAddToPreBalance_balance_below_zero() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> when(currencyServiceLocal.getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash())).thenReturn(baseTransactionData.getCurrencyHash()));
        assertFalse(baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions()));
    }

    @Test
    void checkBalancesAndAddToPreBalance_success() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            if (baseTransactionData instanceof InputBaseTransactionData) {
                Hash addressHash = baseTransactionData.getAddressHash();
                baseNodeBalanceService.balanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
                baseNodeBalanceService.balanceMap.get(addressHash).putIfAbsent(baseTransactionData.getCurrencyHash(), new BigDecimal(10));
                baseNodeBalanceService.preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
                baseNodeBalanceService.preBalanceMap.get(addressHash).putIfAbsent(baseTransactionData.getCurrencyHash(), new BigDecimal(10));
            }
            when(currencyServiceLocal.getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash())).thenReturn(baseTransactionData.getCurrencyHash());
        });
        assertTrue(baseNodeBalanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions()));
    }
}
