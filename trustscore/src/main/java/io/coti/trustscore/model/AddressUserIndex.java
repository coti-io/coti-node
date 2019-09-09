package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.AddressUserIndexData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressUserIndex extends Collection<AddressUserIndexData> {

    public void init() {
        super.init();
    }
}
