package io.coti.zero_spend.monitor.Impl;

import io.coti.common.data.Hash;
import io.coti.zero_spend.monitor.MonitorAccess;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MonitorAccessImpl implements MonitorAccess {

    private Map<Hash, Integer> accessCounters ;


    @PostConstruct
    private void init(){
        accessCounters = new ConcurrentHashMap<>();
    }

    @Override
    public void accessEvent(Hash transactionHash){
        Integer counter = accessCounters.get(transactionHash);
        if(counter != null){
            accessCounters.put(transactionHash, counter+1);
        }
        else {
            accessCounters.put(transactionHash,1);
        }
    }


}
