package io.coti.basenode.controllers;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.validation.Valid;

import java.io.File;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Controller
public class ClusterStampController {

    @Autowired
    private IClusterStampService clusterStampService;

    @RequestMapping(value = "/getLastClusterStamp", method = POST)
    public ResponseEntity<ClusterStampData> getLastClusterStamp(@Valid @RequestBody long totalConfirmedTransactionsPriorClusterStamp) {
        return ResponseEntity.ok(clusterStampService.getNewerClusterStamp(totalConfirmedTransactionsPriorClusterStamp));
    }

    @RequestMapping(value = "/getLastClusterStampSignature", method = POST)
    public ResponseEntity<SignatureData> getLastClusterStampSignature(@Valid @RequestBody long totalConfirmedTransactionsPriorClusterStamp) {
        return ResponseEntity.ok(clusterStampService.getNewerClusterStampSignature(totalConfirmedTransactionsPriorClusterStamp));
    }

    @RequestMapping(value = "/getSignerHash", method = POST)
    public ResponseEntity<Hash> getSignerHash(@Valid @RequestBody long totalConfirmedTransactionsPriorClusterStamp) {
        return ResponseEntity.ok(clusterStampService.getSignerHash(totalConfirmedTransactionsPriorClusterStamp));
    }

    @RequestMapping(value = "/getLastClusterStampFile", method = POST)
    public ResponseEntity<File> getLastClusterStampFile(@Valid @RequestBody long totalConfirmedTransactionsPriorClusterStamp) {
        return ResponseEntity.ok(clusterStampService.getNewerClusterStampFile());
    }


}