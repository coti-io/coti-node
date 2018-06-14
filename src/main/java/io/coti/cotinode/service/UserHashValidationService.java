package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import org.springframework.stereotype.Service;

@Service
public class UserHashValidationService {

    public boolean isLegalHash(Hash hash){
        return true;
    }
}
