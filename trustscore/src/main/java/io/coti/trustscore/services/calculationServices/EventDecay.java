package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.services.calculationServices.interfaces.IEventDecay;


public class EventDecay implements IEventDecay {

    private EventScore eventScore;
    private double eventContributionValue;

    public EventDecay(EventScore eventScore, double eventContributionValue) {

        this.eventScore = eventScore;
        this.eventContributionValue = eventContributionValue;
    }

    @Override
    public EventScore getEventScore() {
        return eventScore;
    }

    @Override
    public double getEventContributionValue() {
        return eventContributionValue;
    }

}
