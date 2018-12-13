package io.coti.financialserver.controllers;

import io.coti.financialserver.http.ItemRequest;
import io.coti.financialserver.http.VoteRequest;
import io.coti.financialserver.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    ItemService itemService;

    public ItemController() {
        itemService = new ItemService();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity updated(@Valid @RequestBody ItemRequest request) {

        return itemService.updated(request);
    }

    @RequestMapping(path = "/vote", method = RequestMethod.PUT)
    public ResponseEntity vote(@Valid @RequestBody VoteRequest request) {

        return itemService.vote(request);
    }
}
