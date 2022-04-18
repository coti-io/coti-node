package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;

import java.nio.charset.StandardCharsets;

public enum NodeFeeType {
    FULL_NODE_FEE,
    NETWORK_FEE,
    TOKEN_GENERATION_FEE,
    TOKEN_MINTING_FEE;

    private Hash hash;

    NodeFeeType() {
        hash = CryptoHelper.cryptoHash(this.name().getBytes(StandardCharsets.UTF_8));
    }

    public Hash getHash() {
        return hash;
    }

}
