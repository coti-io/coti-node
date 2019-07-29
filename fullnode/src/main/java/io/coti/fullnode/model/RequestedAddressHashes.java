package io.coti.fullnode.model;

import io.coti.basenode.model.Collection;
import io.coti.fullnode.data.RequestedAddressHashData;
import org.springframework.stereotype.Service;

@Service
public class RequestedAddressHashes extends Collection<RequestedAddressHashData> {

    @Override
    public void init() {
        super.init();
    }
}
