package io.coti.nodemanager.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.http.SetNewClusterStampsRequest;
import io.coti.nodemanager.services.ClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
@Slf4j
public class ClusterStampController {

    @Autowired
    private ClusterStampService clusterStampService;

    @PostMapping(path = "/newClusterStamp")
    public ResponseEntity<IResponse> setClusterStamps(@Valid @RequestBody SetNewClusterStampsRequest setNewClusterStampsRequest) {
        return clusterStampService.setNewClusterStamps(setNewClusterStampsRequest);
    }

}
