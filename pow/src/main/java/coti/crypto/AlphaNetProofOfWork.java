package coti.crypto;

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

    public AlphaNetProofOfWork(byte[] startOrderSeed, int trustScore) {
        int maxTrustScore = 100;

        if (startOrderSeed.length < 8) {
            throw new IllegalArgumentException("startOrderSeed must be at least 8 bytes long");
        }
        if (trustScore < 0 || trustScore > maxTrustScore) {
            throw new IllegalArgumentException("trustScore must be between 0 and 100 inclusive");
        }

        hashOrder = new AlphaNetOrder(startOrderSeed, trustScore, maxTrustScore);
        hashWorker = new AlphaNetWorker(hashOrder);
    }
}
