package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetCommentsRequest;
import io.coti.financialserver.http.NewCommentRequest;
import io.coti.financialserver.services.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PutMapping()
    public ResponseEntity<IResponse> newComment(@Valid @RequestBody NewCommentRequest request) {

        return commentService.newComment(request);
    }

    @PostMapping()
    public ResponseEntity<IResponse> getComments(@Valid @RequestBody GetCommentsRequest request) {

        return commentService.getComments(request);
    }
}
