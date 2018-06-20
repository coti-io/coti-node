package io.coti.cotinode.config;

import io.coti.cotinode.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionsConfiguration {

    @Bean
    public Transactions transactions(){
        Transactions transactions = new Transactions();
        return transactions;
    }

    @Bean
    public BaseTransactions baseTransactions(){
        BaseTransactions baseTransactions = new BaseTransactions();
        return baseTransactions;
    }

    @Bean
    public Addresses addresses(){
        Addresses addresses = new Addresses();
        return addresses;
    }

    @Bean
    public ConfirmedTransaction balances(){
        ConfirmedTransaction confirmedTransaction = new ConfirmedTransaction();
        return confirmedTransaction;
    }

    @Bean
    public UnconfirmedTransaction preBalances(){
        UnconfirmedTransaction unconfirmedTransaction = new UnconfirmedTransaction();
        return unconfirmedTransaction;
    }
}
