package io.coti.basenode.model;

import io.coti.basenode.data.AddressData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Addresses extends Collection<AddressData> {

    public Addresses() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
