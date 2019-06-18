package io.coti.historynode.model;

import io.coti.basenode.model.Collection;
import io.coti.historynode.data.AddressTransactionsByDatesHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class AddressTransactionsByDatesHistories extends Collection<AddressTransactionsByDatesHistory> {

    public AddressTransactionsByDatesHistories() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}

