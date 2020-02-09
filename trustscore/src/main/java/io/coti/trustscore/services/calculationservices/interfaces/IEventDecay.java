package io.coti.trustscore.services.calculationservices.interfaces;

import io.coti.trustscore.config.rules.EventScore;

public interface IEventDecay {
    EventScore getEventScore();

    double getEventContributionValue();

}
