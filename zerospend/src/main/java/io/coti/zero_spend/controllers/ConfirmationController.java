package io.coti.zero_spend.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm")
public class ConfirmationController {


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> confirmTransaction() {
int x = 5;

        return null;
    }

}
