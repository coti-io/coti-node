package io.coti.trustscore.rulesData;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "behaviorEventsScore")
public class BadEventsScore {
    @XmlElement(name = "standardEventScore")
    private List<BadEventScore> badEventScoreList;
}