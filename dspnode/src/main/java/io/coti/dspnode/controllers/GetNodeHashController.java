package io.coti.dspnode.controllers;

import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/nodeHash")
public class GetNodeHashController {

    @RequestMapping(method = GET)
    public Hash getNodeHash() {
        return NodeCryptoHelper.getNodeHash();
    }
}