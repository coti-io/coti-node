package io.coti.common.pow;

import java.util.List;

public interface IAlgorithmOrder {
    List<IAlgorithm.AlgorithmTypes> getHashingAlgorithms();
    IAlgorithm getHashingAlgorithm(IAlgorithm.AlgorithmTypes algorithm);
}
