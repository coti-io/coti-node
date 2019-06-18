package io.coti.historynode.model;

import io.coti.basenode.model.Collection;
import io.coti.historynode.data.AddressTransactionsByDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class AddressTransactionsByDates extends Collection<AddressTransactionsByDate> {

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }
}
