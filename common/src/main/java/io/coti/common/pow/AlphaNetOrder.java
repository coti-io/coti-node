package io.coti.common.pow;

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

    AlphaNetOrder(byte[] startOrder, int trustScore) {
        int count = trustMapping(trustScore);
        byte[] order = Arrays.copyOf(startOrder, (count + 1) / 2);
        setupAlgorithms(order, count);
    }

    private int trustMapping(int trustScore) {
        int count = 16;
        if (trustScore < 10) {
            count = 13;
        } else if (trustScore < 20) {
            count = 12;
        } else if (trustScore < 30) {
            count = 11;
        } else if (trustScore < 40) {
            count = 10;
        } else if (trustScore < 50) {
            count = 8;
        } else if (trustScore < 60) {
            count = 7;
        } else if (trustScore < 70) {
            count = 6;
        } else if (trustScore < 80) {
            count = 4;
        } else if (trustScore < 90) {
            count = 3;
        } else if (trustScore <= 100) {
            count = 2;
        }
        return count;
    }

    private void setupAlgorithms(byte[] order, int length) {
        IAlgorithm.AlgorithmTypes[] hashArray = IAlgorithm.AlgorithmTypes.values();
        for (int i = 0; i < order.length; i++) {
            setupAlgorithm(hashArray[order[i] >> 4 & 0xF]);
            if (i < order.length - 1 || length % 2 == 0) {
                setupAlgorithm(hashArray[order[i] & 0xF]);
            }
        }
    }

    private void setupAlgorithm(IAlgorithm.AlgorithmTypes algorithm) {
        hashingAlgorithms.add(algorithm);
        algorithmMap.putIfAbsent(algorithm, new AlphaNetAlgorithm(algorithm));
    }
}
