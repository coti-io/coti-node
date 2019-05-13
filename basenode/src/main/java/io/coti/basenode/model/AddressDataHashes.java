package io.coti.basenode.model;

import io.coti.basenode.data.AddressDataHash;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AddressDataHashes extends Collection<AddressDataHash> {

    public AddressDataHashes(){
        // Empty constructor for serialization
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }

}
