package io.coti.basenode.controllers;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/nodeHash")
public class NodeHashController {

    @RequestMapping(method = GET)
    public Hash getNodeHash() {
        return NodeCryptoHelper.getNodeHash();
    }
}