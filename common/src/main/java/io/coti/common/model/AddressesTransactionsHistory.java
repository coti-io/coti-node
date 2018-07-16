package io.coti.common.model;

import io.coti.common.data.AddressTransactionsHistory;

import javax.annotation.PostConstruct;

public class AddressesTransactionsHistory extends Collection<AddressTransactionsHistory> {

        public AddressesTransactionsHistory() {
                }

        @PostConstruct
        public void init() {
                super.init();
        }
}
