package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "highFrequencyEventsScore")
public class DisputedEventScore extends EventScore {

    @XmlElement(name = "idea")
    private String idea;
    @XmlElement(name = "term")
    private int term;

}