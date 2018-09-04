package io.coti.pot.interfaces;

import java.util.List;

public interface IAlgorithmOrder {
    List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms();

    IAlgorithm getHashingAlgorithm(IAlgorithm.AlgorithmTypes algorithm);
}
