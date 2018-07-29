package io.coti.common.pow;

import java.util.List;

public class AlphaNetProofOfWork implements IProofOfWork {
    private IAlgorithmOrder hashOrder;
    private IAlgorithmWorker hashWorker;

    public List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms() {
        return hashOrder.getHashingAlgorithms();
    }

    public int[] hash(byte[] data, byte[] target) {
        return hashWorker.hash(data, target);
    }

    public boolean verify(byte[] data, int[] nonce, byte[] target) {
        return hashWorker.verify(data, nonce, target);
    }

    public AlphaNetProofOfWork(byte[] startOrder, int trustScore) {
        if (startOrder.length != 8) {
            throw new IllegalArgumentException("startOrder must be 8 bytes (16 nibbles) long");
        }
        if (trustScore < 0 || trustScore > 100) {
            throw new IllegalArgumentException("trustScore must be between 0 and 100 inclusive");
        }
        if (IAlgorithm.AlgorithmTypes.values().length != 16) {
            throw new IllegalStateException("Internal implementation of private enum `crypto.coti.IAlgorithm.AlgorithmTypes` must contain 16 items");
        }

        hashOrder = new AlphaNetOrder(startOrder, trustScore);
        hashWorker = new AlphaNetWorker(hashOrder);
    }
}
