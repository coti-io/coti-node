package io.coti.zerospend.controllers;


import io.coti.basenode.services.interfaces.IZeroSpendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ZeroSpendController {


    @Autowired
    private IZeroSpendService zeroSpendService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/message")
    //@SendToUser("/queue/reply")
    public String processMessageFromClient(@Payload String message, Principal principal) {
        return zeroSpendService.getGenesisTransactions();
      //  messagingTemplate.convertAndSendToUser(principal.getName(), "/topic/getGenesisTransactions", );
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}
