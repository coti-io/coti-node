package io.coti.dspnode.controllers;

import afu.org.checkerframework.checker.units.qual.A;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/getNodeHash")
public class GetNodeHashController {

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    @RequestMapping(method = GET)
    public Hash getNodeHash() {
        return new Hash(nodeCryptoHelper.getNodePublicKey());
    }

}
