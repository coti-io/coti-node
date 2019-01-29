package io.coti.historynode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitializationService {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AddressService addressService;
}
