package io.coti.cotinode.controllers;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WSController {



    @SendTo("")
    public String getSomething(String key){

        return "";
    }


}
