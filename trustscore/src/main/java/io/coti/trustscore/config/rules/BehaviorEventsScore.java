package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "behaviorEventsScore")
public class BehaviorEventsScore {
    @XmlElement(name = "standardEventScore")
    private List<BaseEventScore> baseEventScoreList;


    public List<BaseEventScore> getBaseEventScoreList() {
        return baseEventScoreList;
    }

    public Map<BehaviorEventsScoreType, BaseEventScore> getBaseEventScoreMap() {
        return getBaseEventScoreList().stream().collect(
                Collectors.toMap(t -> BehaviorEventsScoreType.enumFromString(t.getName()), t -> t));
    }
}