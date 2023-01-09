package io.coti.basenode.services;


import io.coti.basenode.data.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IMintingService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BaseNodeMintingService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
public class BaseNodeMintingServiceTest {

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
    private ITransactionHelper transactionHelper;
    @MockBean
    private ICurrencyService currencyService;

    @Test
    public void revertMintingAllocation() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Hash currencyHash = TransactionTestUtils.generateRandomHash();
        when(transactionHelper.getTokenMintingFeeData(transactionData)).thenReturn(tokenMintingFeeBaseTransactionData);
        when(tokenMintingFeeBaseTransactionData.getServiceData()).thenReturn(tokenMintingServiceData);
        when(tokenMintingFeeBaseTransactionData.getCurrencyHash()).thenReturn(currencyHash);
        when(tokenMintingFeeBaseTransactionData.getAmount()).thenReturn(new BigDecimal(0));
        when(tokenMintingServiceData.getMintingCurrencyHash()).thenReturn(currencyHash);
        when(currencies.getByHash(currencyHash)).thenReturn(new CurrencyData());
        when(currencyService.getTokenMintableAmount(currencyHash)).thenReturn(new BigDecimal(1000000));
        when(tokenMintingFeeBaseTransactionData.getServiceData().getMintingAmount()).thenReturn(new BigDecimal(0));
        mintingService.revertMintingAllocation(transactionData);
        verify(currencyService, atLeastOnce()).putToMintableAmountMap(currencyHash, new BigDecimal(1000000));
    }
}