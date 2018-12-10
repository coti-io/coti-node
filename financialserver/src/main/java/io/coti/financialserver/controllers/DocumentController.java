package io.coti.financialserver.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import io.coti.financialserver.services.DocumentService;
import io.coti.financialserver.http.DocumentRequest;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Sign;


@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    DocumentService documentService;

    public DocumentController() {
        documentService = new DocumentService();
    }

    @RequestMapping(path = "/new", method = RequestMethod.POST)
    public ResponseEntity newDocument(@Valid @RequestBody DocumentRequest request) {

        return documentService.newDocument(request);
    }

    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public ResponseEntity getDocument(@Valid @RequestBody DocumentRequest request) {

        return documentService.getDocument(request);
    }

    @RequestMapping(path = "/uploadFileToDocument", method = RequestMethod.POST)
    public ResponseEntity uploadFileToDocument(@RequestParam("userHash") Hash userHash,
                                               @RequestParam("disputeHash") Hash disputeHash,
                                               @RequestParam("documentHash") Hash documentHash,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam("r") String r,
                                               @RequestParam("s") String s) {

        SignatureData userSignature = new SignatureData(r, s);
        return documentService.uploadFileToDocument(userHash, disputeHash, documentHash, userSignature, file);
    }

    @RequestMapping(path = "/getFile", method = RequestMethod.POST)
    public void getDocumentFile(@Valid @RequestBody DocumentRequest request, HttpServletResponse response) throws IOException {

        documentService.getDocumentFile(request, response);
        response.flushBuffer();
    }
}
