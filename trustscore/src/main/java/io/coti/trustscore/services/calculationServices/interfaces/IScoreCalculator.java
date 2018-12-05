package io.coti.trustscore.services.calculationServices.interfaces;

import io.coti.trustscore.config.rules.EventScore;

import java.util.Map;

public interface IScoreCalculator {
    <T extends EventScore, S> Map<T, S> calculate();
}
