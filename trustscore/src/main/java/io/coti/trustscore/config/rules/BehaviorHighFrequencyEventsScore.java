package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "behaviorHighFrequencyEventsScore")
public class BehaviorHighFrequencyEventsScore {

    @XmlElement(name = "highFrequencyEventsScore")
    private List<HighFrequencyEventScore> highFrequencyEventsScoreList;

    public List<HighFrequencyEventScore> getHighFrequencyEventScoreList() {
        return highFrequencyEventsScoreList;
    }

    public Map<HighFrequencyEventScoreType, HighFrequencyEventScore> getHighFrequencyEventScoreMap() {
        return highFrequencyEventsScoreList.stream().collect(
                Collectors.toMap(t -> HighFrequencyEventScoreType.enumFromString(t.getName()), t -> t));
    }
}