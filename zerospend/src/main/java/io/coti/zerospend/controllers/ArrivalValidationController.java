package io.coti.zerospend.controllers;

import io.coti.basenode.controllers.BaseMessageArrivalValidationController;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.http.GetMissedDataHashesResponse;
import io.coti.zerospend.services.MessageArrivalValidationRouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;

@Slf4j
@Controller
public class ArrivalValidationController implements BaseMessageArrivalValidationController {

    @Autowired
    private MessageArrivalValidationRouterService messageArrivalValidationRouterService;

    @Override
    public ResponseEntity<GetMissedDataHashesResponse> getMissedDataHashes(MessageArrivalValidationData data) {
        log.info("Received getMissedDataHashes request");
        try {
            MessageArrivalValidationData missedMessagesHashes = messageArrivalValidationRouterService.getMissedMessageHashes(data);
            if (!missedMessagesHashes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(new GetMissedDataHashesResponse(missedMessagesHashes, "Request successful", STATUS_SUCCESS));
            }
            //TODO 5/12/2019 astolia: check if it is correct ot return error if missedMessagesHashes is empty
            else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetMissedDataHashesResponse(new MessageArrivalValidationData(), "Request failed", STATUS_ERROR));
            }
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GetMissedDataHashesResponse(
                            new MessageArrivalValidationData(),
                            "Internal Server Error",
                            STATUS_ERROR));
        }
    }
}