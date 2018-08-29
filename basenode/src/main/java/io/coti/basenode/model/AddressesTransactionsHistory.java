package io.coti.basenode.model;

import io.coti.basenode.data.AddressTransactionsHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class AddressesTransactionsHistory extends Collection<AddressTransactionsHistory> {

    public AddressesTransactionsHistory() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
