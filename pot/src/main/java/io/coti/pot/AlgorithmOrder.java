package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm;
import io.coti.pot.interfaces.IAlgorithmOrder;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class AlgorithmOrder implements IAlgorithmOrder {

    private final List<IAlgorithm.AlgorithmType> hashingAlgorithms;
    private final Map<IAlgorithm.AlgorithmType, IAlgorithm> algorithmMap = new EnumMap<>(IAlgorithm.AlgorithmType.class);

    AlgorithmOrder(int trustScore, int maxTrustScore) {
        IAlgorithm.AlgorithmType[] allAlgos = IAlgorithm.AlgorithmType.values();
        List<IAlgorithm.AlgorithmType> allAlgosList = Arrays.asList(allAlgos);

        int count = getAlgorithmCount(trustScore, maxTrustScore, allAlgosList.size());
        hashingAlgorithms = allAlgosList.subList(0, count);

        hashingAlgorithms.forEach(algorithm -> algorithmMap.putIfAbsent(algorithm, new Algorithm(algorithm)));
    }

    public List<IAlgorithm.AlgorithmType> getHashingAlgorithms() {
        return this.hashingAlgorithms;
    }

    public IAlgorithm getHashingAlgorithm(IAlgorithm.AlgorithmType algorithm) {
        return this.algorithmMap.get(algorithm);
    }

    private int getAlgorithmCount(int trustScore, int maxTrustScore, int maxLength) {
        if (trustScore == 0) return maxLength;
        return (int) ((maxLength + 1) - ((trustScore / (double) maxTrustScore) * maxLength));
    }
}
