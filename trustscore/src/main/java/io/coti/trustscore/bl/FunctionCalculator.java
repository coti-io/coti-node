package io.coti.trustscore.bl;

import io.coti.trustscore.rulesData.EventScore;
import java.util.Map;

public interface  FunctionCalculator {
    <T extends EventScore, S> Map<T, S> calculate();
}
