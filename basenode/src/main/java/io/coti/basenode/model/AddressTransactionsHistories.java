package io.coti.basenode.model;

import io.coti.basenode.data.AddressTransactionsHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressTransactionsHistories extends Collection<AddressTransactionsHistory> {

    public AddressTransactionsHistories() {
    }

    public void init() {
        super.init();
    }
}
