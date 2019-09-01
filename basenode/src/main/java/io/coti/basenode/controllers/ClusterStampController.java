package io.coti.basenode.controllers;

import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class ClusterStampController {

    @Autowired
    private IClusterStampService clusterStampService;

    @GetMapping(value = "/clusterstamps")
    public ResponseEntity<GetClusterStampFileNamesResponse> getRequiredClusterStampNames(){
        return clusterStampService.getRequiredClusterStampNames();
    }

}
