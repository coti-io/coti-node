package io.coti.trustscore.bl.Decays;

import io.coti.trustscore.rulesData.EventScore;

import java.util.Map;

public interface IDecayCalculator {
    <T extends EventScore, S> Map<T, S> calculate(int numberOfDecays);
}


