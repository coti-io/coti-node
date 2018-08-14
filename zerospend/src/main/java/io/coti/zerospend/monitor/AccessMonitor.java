package io.coti.zerospend.monitor;

import io.coti.common.data.Hash;
import io.coti.zerospend.monitor.interfaces.IAccessMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class AccessMonitor implements IAccessMonitor {


    @Value("${zerospend.request.limit}")
    private int requestLimit;

    private Map<Hash, Integer> accessCounters;

    @PostConstruct
    private void init() {
        accessCounters = new ConcurrentHashMap<>();
    }

    @Override
    public boolean insertAndValidateAccessEvent(Hash transactionHash) {
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

    public Map<Hash, Integer> getAccessCounters(){
        return accessCounters;
    }


}
