package io.coti.trustscore.rulesData;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "initialTrustScore")
public class InitialTrustScore {
    @XmlElement(name = "initialTrustScoreComponent")
    private List<Component> componentList;
}