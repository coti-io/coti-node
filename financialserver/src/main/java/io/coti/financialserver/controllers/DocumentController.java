package io.coti.financialserver.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import io.coti.financialserver.services.DocumentService;
import io.coti.financialserver.http.GetDocumentRequest;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentController {

    private DocumentService documentService;

    public DocumentController() {
        documentService = new DocumentService();
    }

    @RequestMapping(path = "/new", method = RequestMethod.POST)
    public ResponseEntity newDocument(@RequestParam("userHash") Hash userHash,
                                      @RequestParam("disputeId") Integer disputeId,
                                      @RequestParam("documentId") Integer documentId,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("r") String r,
                                      @RequestParam("s") String s) {
        SignatureData signature = new SignatureData(r, s);
        return documentService.newDocument(userHash, disputeId, documentId, file, signature);
    }

    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public void getDocument(@Valid @RequestBody GetDocumentRequest request, HttpServletResponse response) throws IOException {
        documentService.getDocument(request.getUserHash(), request.getDisputeId(), request.getDocumentId(), request.getSignature(), response);
    }
}
