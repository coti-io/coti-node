package io.coti.zerospend.services;

import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {
    public static TransactionCreationService transactionCreationService;
    public static TransactionCryptoCreator transactionCryptoCreator;
    public static SourceStarvationService sourceStarvationService;

    @Autowired
    public TransactionCreationService autowiredTransactionCreationService;
    @Autowired
    public TransactionCryptoCreator autowiredTransactionCryptoCreator;
    @Autowired
    public SourceStarvationService autowiredSourceStarvationService;
}
