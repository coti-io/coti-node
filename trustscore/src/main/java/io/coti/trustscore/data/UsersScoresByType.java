package io.coti.trustscore.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "users")
public class UsersScoresByType {
    @XmlElement(name = "user")
    private List<UserScoresByType> userScoresByTypeList;
}

@XmlRootElement(name = "user")
class UserScoresByType {
    @XmlAttribute(name = "type")
    private String type;
    @XmlElement(name = "initialTrustScore")
    private InitialTrustScore initialTrustScore;
    @XmlElement(name = "transactionEventsScore")
    private TransactionEventsScore transactionEventScoreList;
    @XmlElement(name = "badEventsScore")
    private BadEventsScore badEventScoreList;
    @XmlElement(name = "disputedEventsScore")
    private DisputedEventsScore disputedEventScoreList;
}

@XmlRootElement(name = "initialTrustScore")
class InitialTrustScore {
    @XmlElement(name = "component")
    private List<Component> componentList;
}

@XmlRootElement(name = "component")
class Component {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "definition")
    private String definition;
    @XmlElement(name = "range")
    private Range range;
    @XmlElement(name = "weight")
    private double weight;
    @XmlElement(name = "decay")
    private String decay;
}

@XmlRootElement(name = "transactionEventsScore")
class TransactionEventsScore {
    @XmlElement(name = "transactionEventScore")
    private List<TransactionEventScore> transactionEventScoreList;
}

@XmlRootElement(name = "transactionEventScore")
class TransactionEventScore {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "idea")
    private String idea;
    @XmlElement(name = "nonlinearFunction")
    private String nonlinearFunction;
    @XmlElement(name = "weight")
    private double weight;
    @XmlElement(name = "decay")
    private String decay;
}

@XmlRootElement(name = "range")
class Range {
    @XmlElement(name = "from")
    private int from;
    @XmlElement(name = "to")
    private int to;
}

@XmlRootElement(name = "badEventsScore")
class BadEventsScore {
    @XmlElement(name = "badEventScore")
    private List<BadEventScore> badEventScoreList;
}

@XmlRootElement(name = "badEventScore")
class BadEventScore {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "definition")
    private String definition;
    @XmlElement(name = "term")
    private int term;
    @XmlElement(name = "weight")
    private double weight;
    @XmlElement(name = "decay")
    private String decay;

}

@XmlRootElement(name = "disputedEventsScore")
class DisputedEventsScore {
    @XmlElement(name = "disputedEventScore")
    private List<DisputedEventScore> disputedEventScoreList;
}

@XmlRootElement(name = "disputedEventScore")
class DisputedEventScore {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "idea")
    private String idea;
    @XmlElement(name = "term")
    private int term;
    @XmlElement(name = "weight")
    private double weight;
    @XmlElement(name = "decay")
    private String decay;

}



