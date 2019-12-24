package io.coti.trustscore.data.tsenums;

import io.coti.trustscore.data.tsevents.*;

public enum EventRequestType {
    FALSEQUESTIONNAIRE("FALSEQUESTIONNAIRE", FalseQuestionnaireBehaviorEventData.class),
    DOUBLESPENDING("DOUBLESPENDING", DoubleSpendingBehaviorEventData.class),
    INVALIDTX("INVALIDTX", InvalidTxBehaviorEventData.class),
    CLAIM("CLAIM", ClaimFrequencyBasedEventData.class);

    private String text;
    private Class eventClass;

    EventRequestType(String text , Class eventClass) {
        this.text = text;
        this.eventClass = eventClass;
    }

    @Override
    public String toString() {
        return text;
    }

    public Class getEventClass(){
        return eventClass;
    }
}
