package io.coti.trustscore.bl;

import io.coti.trustscore.rulesData.EventScore;
import java.util.Map;

public interface DecayCalculator {
    <T extends EventScore, S> Map<T, S> calculate(int numberOfDecays);
}


