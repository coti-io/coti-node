package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "behaviorHighFrequencyEventsScore")
public class DisputedEventsScore {
    @XmlElement(name = "highFrequencyEventsScore")
    private List<DisputedEventScore> disputedEventScoreList;
}
