package io.coti.storagenode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitializationService {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AddressService addressService;

}
