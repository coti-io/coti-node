package io.coti.common.pow;


import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Slf4j
class AlphaNetAlgorithm implements IAlgorithm {

    private MessageDigest hashingAlgorithm;

    public AlphaNetAlgorithm(IAlgorithm.AlgorithmTypes hashingAlgorithm) {
        try {
            this.hashingAlgorithm = MessageDigest.getInstance(hashingAlgorithm.toString());
        } catch (NoSuchAlgorithmException e) {
            log.error("exception when choosing digest algorithm",e);

        }
    }

    @Override
    public byte[] hash(byte[] input) {
        return hashingAlgorithm.digest(input);
    }
}
