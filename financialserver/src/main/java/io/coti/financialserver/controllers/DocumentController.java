package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetDocumentFileRequest;
import io.coti.financialserver.http.GetDocumentNamesRequest;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import io.coti.financialserver.services.DocumentService;
import io.coti.financialserver.http.NewDocumentRequest;

@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    DocumentService documentService;

    @RequestMapping(path="/upload", method = RequestMethod.POST)
    public ResponseEntity<IResponse> newDocument(@ModelAttribute @Valid NewDocumentRequest request) {
        return documentService.newDocument(request);
    }

    @RequestMapping(path = "/names",method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDocumentNames(@Valid GetDocumentNamesRequest request) {

        return documentService.getDocumentNames(request);
    }

    @RequestMapping(path= "/download", method = RequestMethod.POST)
    public void getDocumentFile(@Valid @RequestBody GetDocumentFileRequest request, HttpServletResponse response) throws IOException {

        documentService.getDocumentFile(request, response);
        response.flushBuffer();
    }
}
