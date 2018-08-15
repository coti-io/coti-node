package coti.crypto;

import java.nio.ByteBuffer;
import java.util.*;

class AlphaNetOrder implements IAlgorithmOrder {

    private final List<IAlgorithm.AlgorithmTypes> hashingAlgorithms = new ArrayList<>();

    public List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms() {
        return this.hashingAlgorithms;
    }

    private final Map<IAlgorithm.AlgorithmTypes, IAlgorithm> algorithmMap = new HashMap<>();

    public IAlgorithm getHashingAlgorithm(IAlgorithm.AlgorithmTypes algorithm) {
        return this.algorithmMap.get(algorithm);
    }

    AlphaNetOrder(byte[] startOrderSeed, int trustScore, int maxTrustScore) {
        IAlgorithm.AlgorithmTypes[] allAlgos = IAlgorithm.AlgorithmTypes.values();
        List<IAlgorithm.AlgorithmTypes> allAlgosList = Arrays.asList(allAlgos);

        int count = getAlgorithmCount(trustScore, maxTrustScore, allAlgosList.size());
        List<IAlgorithm.AlgorithmTypes> hashes = allAlgosList.subList(0, count);
        Collections.shuffle(hashes, new Random(ByteBuffer.wrap(startOrderSeed).getLong()));

        for (IAlgorithm.AlgorithmTypes algorithm: hashes) {
            hashingAlgorithms.add(algorithm);
            algorithmMap.putIfAbsent(algorithm, new AlphaNetAlgorithm(algorithm));
        }
    }

    private int getAlgorithmCount(int trustScore, int maxTrustScore, int maxLength) {
        if (trustScore == 0) return maxLength;
        return (int)((maxLength + 1) - ((trustScore / (double)maxTrustScore) * maxLength));
    }
}
