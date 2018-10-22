package io.coti.trustscore.bl.ScoreFunctionCalculation;

import io.coti.trustscore.rulesData.EventScore;

import java.util.Map;

public interface IFunctionCalculator {
    <T extends EventScore, S> Map<T, S> calculate();
}
