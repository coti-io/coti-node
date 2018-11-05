package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.DisputeEventData;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.config.rules.BaseEventScore;
import io.coti.trustscore.config.rules.BehaviorEventsScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.utils.DatesCalculation;
import io.coti.trustscore.utils.MathCalculation;
import org.mariuszgromada.math.mxparser.Argument;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EventsDecayCalculator {


    private Map<UserType, Map<EventType, BaseEventScore>> eventsScoreMapping  = new HashMap<>();

    private Map<EventType,Function> getValueEventMapper = new HashMap<EventType,Function>(){{
            put(EventType.DISPUTE, e-> new Argument("X",((DisputeEventData)e).getTransactionData().getAmount().doubleValue()));

    }};

    private RulesData rulesData;

    public void init(RulesData rulesData) {



        this.rulesData = rulesData;
        eventsScoreMapping  = rulesData.getUsersRules().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e-> mapBaseEvents(e.getValue().getBehaviorEventsScore())));
    }

    public Map<EventType, BaseEventScore> mapBaseEvents(BehaviorEventsScore behaviorEventsScore){
        return behaviorEventsScore.getBaseEventScoreList().stream().collect( Collectors.toMap(e-> EventType.valueOf(e.getName()),e-> e));
    }

    public double CalculateDispute(UserType userType,  List<EventData> disputeEventDataList){
        return calculateEventScore(userType,EventType.DISPUTE,disputeEventDataList);
    }





    private double calculateEventScore(UserType userType ,EventType eventType , List<EventData> eventDataList){
        BaseEventScore baseEventScore = eventsScoreMapping.get(userType).get(eventType);


        Date currentDate = new Date();
        double eventScore = eventDataList.stream().mapToDouble(e->
            DatesCalculation.calculateDaysDiffBetweenDates(currentDate,e.getEventDate()) *
                    MathCalculation.evaluteExpression(baseEventScore.getDecay(),  (Argument) getValueEventMapper.get(eventType).apply(e))).sum() *
                baseEventScore.getWeight();
        return eventScore;
    }

}
