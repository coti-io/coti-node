package io.coti.historynode.model;

import io.coti.basenode.model.Collection;
import io.coti.historynode.data.AddressTransactionsByAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressTransactionsByAddresses extends Collection<AddressTransactionsByAddress> {

    @Override
    public void init() {
        super.init();
    }

}
