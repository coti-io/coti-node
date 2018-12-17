package io.coti.financialserver.controllers;

import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.coti.financialserver.http.NewCommentRequest;
import io.coti.financialserver.services.CommentService;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    CommentService commentService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity newComment(@Valid @RequestBody NewCommentRequest request) {

        return commentService.newComment(request);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity getComment(@Valid @RequestBody NewCommentRequest request) {

        return commentService.getComment(request);
    }
}
