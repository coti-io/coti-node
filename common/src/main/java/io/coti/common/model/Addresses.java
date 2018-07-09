package io.coti.common.model;

import io.coti.common.data.AddressData;
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
