package io.coti.nodemanager.controllers;

import io.coti.basenode.data.Network;
import io.coti.nodemanager.services.NodesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodesController {

    @Autowired
    private NodesService nodesService;

    @RequestMapping(path = "/newZerospend", method = RequestMethod.POST)
    public void newZeroSpend(@Valid @RequestBody String zeroSpendAddress) {
        nodesService.newZeroSpendServer(zeroSpendAddress);
    }

    @RequestMapping(path = "/newDsp", method = RequestMethod.POST)
    public void newDspNode(@Valid @RequestBody String dspNodeAddress) {
        nodesService.newDspNode(dspNodeAddress);
    }

    @RequestMapping(path = "/zerospend", method = RequestMethod.GET)
    public String getZeroSpendAddress(){
        return nodesService.getZeroSpendAddress();
    }

    @RequestMapping(path = "/all", method = RequestMethod.GET)
    public Network getAllNodes() {
        return nodesService.getDsps();
    }
}