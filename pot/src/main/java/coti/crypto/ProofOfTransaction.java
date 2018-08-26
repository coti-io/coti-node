package coti.crypto;

import java.util.List;

public class ProofOfTransaction implements IProofOfTransaction {
    private IAlgorithmOrder hashOrder;
    private IAlgorithmWorker hashWorker;

    static int maxTrustScore = 100;

    public List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms() {
        return hashOrder.getHashingAlgorithms();
    }

    public int[] hash(byte[] data, byte[] target) {
        return hashWorker.hash(data, target);
    }
    public boolean verify(byte[] data, int[] nonce, byte[] target) {
        return hashWorker.verify(data, nonce, target);
    }

    public ProofOfTransaction( int trustScore) {

        if (trustScore < 0 || trustScore > maxTrustScore) {
            throw new IllegalArgumentException("trustScore must be between 0 and 100 inclusive");
        }
        hashOrder = new AlgorithmOrder(trustScore, maxTrustScore);
        hashWorker = new AlgorithmWorker(hashOrder);
    }
}
