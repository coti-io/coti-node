package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.GetTokenBalancesResponse;
import io.coti.basenode.services.interfaces.ICurrencyService;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static io.coti.basenode.utils.HashTestUtils.generateRandomAddressHash;

@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeBalanceService.class})

//@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeBalanceServiceTest {

    @Autowired
    private BaseNodeBalanceService balanceService;
    @MockBean
    protected ICurrencyService currencyService;

    @Before
    public void init() {
        balanceService.init();
    }

    @Test
    public void getCurrencyBalances_noNative_valuesMatch() {
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
        balanceService.preBalanceMap.put(addressHash2, currencyHashToAmountMap2);
        balanceService.preBalanceMap.put(addressHash1, currencyHashToAmountMap1);
        ResponseEntity<GetTokenBalancesResponse> currencyBalances = balanceService.getTokenBalances(getCurrencyBalanceRequest);

        Assert.assertEquals(HttpStatus.OK, currencyBalances.getStatusCode());
        Assert.assertTrue(currencyBalances.getBody().getTokenBalances().containsKey(tokenHash1));
        Assert.assertTrue(currencyBalances.getBody().getTokenBalances().containsKey(tokenHash2));
        Assert.assertEquals(BigDecimal.TEN, currencyBalances.getBody().getTokenBalances().get(tokenHash1).get(addressHash1).getAddressBalance());
    }

}
