package io.coti.common.pow;

public interface IAlgorithmWorker {
    int[] hash(byte[] data, byte[] target);
    boolean verify(byte[] data, int[] nonce, byte[] target);
}
