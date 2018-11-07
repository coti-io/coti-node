package io.coti.trustscore.config.rules;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "initialTrustScore")
public class InitialTrustScore {
    @XmlElement(name = "initialTrustScoreComponent")
    private List<Component> componentList;


    public List<Component> getComponentList() {
        return componentList;
    }


    public Component getComponentByType(InitialTrustType initialTrustType) {
        return componentList.stream().filter(e -> e.getName().equals(initialTrustType.name())).findFirst().get();
    }
}