package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.HttpStringConstants;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodesController {

    @Autowired
    private INodesManagementService nodesService;

    @RequestMapping(path = "/new_node", method = RequestMethod.POST)
    public ResponseEntity<String> newNode(@Valid @RequestBody NetworkNodeData networkNodeData) {
        try {
            nodesService.newNode(networkNodeData);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HttpStringConstants.VALIDATION_EXCEPTION_MESSAGE);
        }
        return ResponseEntity.ok(HttpStringConstants.STATUS_SUCCESS);
    }

    @RequestMapping(path = "/all", method = RequestMethod.GET)
    public NetworkDetails getAllNodes() {
        return nodesService.getAllNetworkData();
    }


}