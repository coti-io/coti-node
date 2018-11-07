package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "behaviorEventsScore")
public class BehaviorEventsScore {
    @XmlElement(name = "standardEventScore")
    private List<BaseEventScore> baseEventScoreList;


    public List<BaseEventScore> getBaseEventScoreList() {
        return baseEventScoreList;
    }
}