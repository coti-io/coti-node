package io.coti.pot.interfaces;

import java.util.List;

public interface IProofOfTrust {
    List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms();

    int[] hash(byte[] data, byte[] target);

    boolean verify(byte[] data, int[] nonce, byte[] target);
}
