package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetCommentsRequest;
import io.coti.financialserver.http.NewCommentRequest;
import io.coti.financialserver.services.CommentService;
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
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> newComment(@Valid @RequestBody NewCommentRequest request) {

        return commentService.newComment(request);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> getComments(@Valid @RequestBody GetCommentsRequest request) {

        return commentService.getComments(request);
    }
}
