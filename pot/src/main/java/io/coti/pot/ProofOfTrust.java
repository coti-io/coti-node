package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm;
import io.coti.pot.interfaces.IAlgorithmOrder;
import io.coti.pot.interfaces.IAlgorithmWorker;
import io.coti.pot.interfaces.IProofOfTrust;

import java.util.List;

public class ProofOfTrust implements IProofOfTrust {

    private static final int MAX_TRUST_SCORE = 100;
    private final IAlgorithmOrder hashOrder;
    private final IAlgorithmWorker hashWorker;

    public ProofOfTrust(int trustScore) {

        if (trustScore < 0 || trustScore > MAX_TRUST_SCORE) {
            throw new IllegalArgumentException("trustScore must be between 0 and 100 inclusive");
        }
        hashOrder = new AlgorithmOrder(trustScore, MAX_TRUST_SCORE);
        hashWorker = new AlgorithmWorker(hashOrder);
    }

    public List<IAlgorithm.AlgorithmType> getHashingAlgorithms() {
        return hashOrder.getHashingAlgorithms();
    }

    public int[] hash(byte[] data, byte[] target) {
        return hashWorker.hash(data, target);
    }

    public boolean verify(byte[] data, int[] nonce, byte[] target) {
        return hashWorker.verify(data, nonce, target);
    }
}
