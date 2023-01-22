package io.coti.basenode.services;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.data.TokenMintingServiceData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IMintingService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static io.coti.basenode.services.BaseNodeServiceManager.currencyService;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BaseNodeMintingService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeMintingServiceTest {

    @Autowired
    private IMintingService mintingService;
    @MockBean
    protected Currencies currencies;
    @MockBean
    protected IBalanceService balanceService;
    @MockBean
    private TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData;
    @MockBean
    private TokenMintingServiceData tokenMintingServiceData;
    @MockBean
    private BaseNodeTransactionHelper transactionHelper;
    @MockBean
    private ICurrencyService currencyServiceLocal;
    @MockBean
    INetworkService networkService;

    @BeforeEach
    void init() {
        nodeTransactionHelper = transactionHelper;
        currencyService = currencyServiceLocal;
    }

    @Test
    void revertMintingAllocation() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Hash currencyHash = TransactionTestUtils.generateRandomHash();
        when(transactionHelper.getTokenMintingFeeData(transactionData)).thenReturn(tokenMintingFeeBaseTransactionData);
        when(tokenMintingFeeBaseTransactionData.getServiceData()).thenReturn(tokenMintingServiceData);
        when(tokenMintingServiceData.getMintingCurrencyHash()).thenReturn(currencyHash);
        when(currencyService.getTokenMintableAmount(currencyHash)).thenReturn(new BigDecimal(1000000));
        when(tokenMintingFeeBaseTransactionData.getServiceData().getMintingAmount()).thenReturn(new BigDecimal(0));
        mintingService.revertMintingAllocation(transactionData);
        verify(currencyService, atLeastOnce()).putToMintableAmountMap(currencyHash, new BigDecimal(1000000));
    }
}