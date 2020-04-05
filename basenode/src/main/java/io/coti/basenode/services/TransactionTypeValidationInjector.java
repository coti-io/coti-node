package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransactionTypeValidationInjector {

    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;

    @PostConstruct
    public void init() {
        TransactionTypeValidation.TOKEN_GENERATION_FEE.originatorCurrencyCrypto = originatorCurrencyCrypto;
        TransactionTypeValidation.TOKEN_GENERATION_FEE.currencyTypeRegistrationCrypto = currencyTypeRegistrationCrypto;
    }
}
