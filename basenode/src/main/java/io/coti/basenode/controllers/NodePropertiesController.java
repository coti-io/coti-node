package io.coti.basenode.controllers;

import io.coti.basenode.http.NodeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/nodeProperties")
public class NodePropertiesController {

    @RequestMapping(method = GET)
    public NodeProperties getNodeProperties() {
        NodeProperties nodeProperties = new NodeProperties();
        nodeProperties.setPropagationAddress("tcp://localhost:5002");
        return nodeProperties;
    }
}