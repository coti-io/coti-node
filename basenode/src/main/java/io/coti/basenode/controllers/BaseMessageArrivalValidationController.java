package io.coti.basenode.controllers;

import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.http.GetMissedDataHashesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface BaseMessageArrivalValidationController {

    @PostMapping(value = "/missedDataHashes")
    ResponseEntity<GetMissedDataHashesResponse> getMissedDataHashes(@RequestBody MessageArrivalValidationData data);
}
