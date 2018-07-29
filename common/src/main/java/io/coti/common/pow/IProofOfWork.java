package io.coti.common.pow;

import java.util.List;

public interface IProofOfWork {
    List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms();
    int[] hash(byte[] data, byte[] target);
    boolean verify(byte[] data, int[] nonce, byte[] target);
}
