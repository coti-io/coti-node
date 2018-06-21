package io.coti.cotinode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("io.coti.cotinode")
@EnableScheduling
public class AppConfig {

    private static final String CONFIRMED_TRANSACTIONS_COLOUMN_FAMILY_NAME = "ConfirmedTransaction";

    private static final String UNCONFIRMED_TRANSACTIONS_COLOUMN_FAMILY_NAME = "UnconfirmedTransaction";

    public String getUnconfirmedTransactionsColoumnFamilyName() {
        return UNCONFIRMED_TRANSACTIONS_COLOUMN_FAMILY_NAME;
    }

    public String getConfirmedTransactionsColoumnFamilyName() {
        return CONFIRMED_TRANSACTIONS_COLOUMN_FAMILY_NAME;
    }



}
