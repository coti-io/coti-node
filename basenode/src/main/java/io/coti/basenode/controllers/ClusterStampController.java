package io.coti.basenode.controllers;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetClusterStampFileNames;
import io.coti.basenode.model.ClusterStampNames;
import io.coti.basenode.services.interfaces.IClusterStampService;
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
    private IClusterStampService clusterStampService;

    //TODO 8/14/2019 astolia: remove
    @Autowired
    private ClusterStampNames clusterStampNames;

    @PostMapping(value = "/clusterstamps")
    public ResponseEntity<GetClusterStampFileNames> getRequiredClusterStampNames(@Valid @RequestBody GetClusterStampFileNames getClusterStampFileNames){

        //TODO 8/14/2019 astolia:delete - this is just for testing
        //
        ClusterStampNameData major = new ClusterStampNameData("clusterstamp_m_1565787205728.csv");
        major.setHash(new Hash("aaaaaaaaaaaaaaaa"));

        ClusterStampNameData token1 = new ClusterStampNameData("clusterstamp_t_1565787205728_1565787268212.csv");
        token1.setHash(new Hash("aaaaaaaaaaaaaaab"));

        ClusterStampNameData token2 = new ClusterStampNameData("clusterstamp_t_1565787205728_1565787834293.csv");
        token2.setHash(new Hash("aaaaaaaaaaaaaaac"));

        clusterStampNames.put(major);
        clusterStampNames.put(token1);
        clusterStampNames.put(token2);
        //

        return clusterStampService.getRequiredClusterStampNames(getClusterStampFileNames);
    }

}
