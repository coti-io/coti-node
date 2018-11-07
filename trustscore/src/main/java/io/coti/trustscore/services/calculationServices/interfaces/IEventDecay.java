package io.coti.trustscore.services.calculationServices.interfaces;

import io.coti.trustscore.config.rules.EventScore;

public interface IEventDecay {
    EventScore getEventScore();

    double getEventContributionValue();

}
