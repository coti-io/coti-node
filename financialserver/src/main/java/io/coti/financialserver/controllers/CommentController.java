package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetCommentsRequest;
import io.coti.financialserver.http.NewCommentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.commentService;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    @PutMapping()
    public ResponseEntity<IResponse> newComment(@Valid @RequestBody NewCommentRequest request) {

        return commentService.newComment(request);
    }

    @PostMapping()
    public ResponseEntity<IResponse> getComments(@Valid @RequestBody GetCommentsRequest request) {

        return commentService.getComments(request);
    }
}
