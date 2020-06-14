package io.coti.pot.interfaces;

import java.util.List;

public interface IAlgorithmOrder {
    List<IAlgorithm.AlgorithmType> getHashingAlgorithms();

    IAlgorithm getHashingAlgorithm(IAlgorithm.AlgorithmType algorithm);
}
