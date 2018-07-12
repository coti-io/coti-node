package io.coti.zero_spend.monitor;

import io.coti.common.data.Hash;
import io.coti.zero_spend.monitor.interfaces.IMonitorAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class AccessMonitor implements IMonitorAccess {


    @Value("${zerospend.request.limit}")
    private int requestLimit;

    private Map<Hash, Integer> accessCounters;

    @PostConstruct
    private void init() {
        accessCounters = new ConcurrentHashMap<>();
    }

    @Override
    public boolean validateAccessEvent(Hash transactionHash) {
        Integer counter = accessCounters.get(transactionHash);
        if (counter != null) {
            if (counter > requestLimit) {
                log.error("The node {} reached the limit of zeroSpend requests which is {}"
                        ,transactionHash,requestLimit);
               // throw new Exception("The node " + transactionHash + " reached the limit of zeroSpend requests which is " + zeroSpendRequestLimit);
                return false;
            }
            accessCounters.put(transactionHash, counter + 1);
        } else {
            accessCounters.put(transactionHash, 1);
        }
        return true;
    }


}
