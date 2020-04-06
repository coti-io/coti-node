package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.crypto.TokenMintingCrypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransactionTypeValidationInjector {

    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private TokenMintingCrypto tokenMintingCrypto;

    @PostConstruct
    public void init() {
        TransactionTypeValidation.TOKEN_GENERATION.originatorCurrencyCrypto = originatorCurrencyCrypto;
        TransactionTypeValidation.TOKEN_GENERATION.currencyTypeRegistrationCrypto = currencyTypeRegistrationCrypto;
        TransactionTypeValidation.TOKEN_MINTING.tokenMintingCrypto = tokenMintingCrypto;
    }
}
