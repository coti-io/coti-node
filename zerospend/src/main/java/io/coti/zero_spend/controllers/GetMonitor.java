package io.coti.zero_spend.controllers;

import io.coti.common.data.Hash;
import io.coti.zero_spend.http.MonitorElement;
import io.coti.zero_spend.monitor.interfaces.IAccessMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequestMapping("/monitor")
@RestController
public class GetMonitor {
    private IAccessMonitor monitorAccess;


    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MonitorElement>> getMonitor(){

        List<MonitorElement> monitorElements = new LinkedList<>();
        for(Map.Entry<Hash, Integer> entry : monitorAccess.getAccessCounters().entrySet()){
            monitorElements.add(new MonitorElement(entry.getKey(),entry.getValue()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(monitorElements);

    }


}
