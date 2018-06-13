package io.coti.cotinode.service;

import org.springframework.stereotype.Service;

@Service
public class UserHashValidationService {


    public boolean isLegalHash(byte[] hash){
        return true;
    }
}
