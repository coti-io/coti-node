package io.coti.cotinode.controllers;

import io.coti.cotinode.http.interfaces.NodeInformationResponse;
import io.coti.cotinode.service.interfaces.INodeInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/nodeinformation")
public class NodeInformationController {
    @Autowired
    private INodeInformationService nodeInformationService;

    @RequestMapping(method = GET)
    @ResponseBody
    public NodeInformationResponse getNodeInfo() {

        return nodeInformationService.getNodeInformation();
    }
}