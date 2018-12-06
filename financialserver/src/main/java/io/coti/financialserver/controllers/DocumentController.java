package io.coti.financialserver.controllers;

import io.coti.financialserver.crypto.GetDocumentCrypto;
import io.coti.financialserver.crypto.NewDocumentCrypto;
import io.coti.financialserver.data.NewDocumentData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    private static final String UNAUTHORIZED = "Unauthorized";

    private DocumentService documentService;

    public DocumentController() {
        documentService = new DocumentService();
    }

    @RequestMapping(path = "/new", method = RequestMethod.POST)
    public ResponseEntity newDocument(@RequestParam("userHash") Hash userHash,
                                      @RequestParam("disputeHash") Hash disputeHash,
                                      @RequestParam("itemId") long itemId,
                                      @RequestParam("name") String name,
                                      @RequestParam("description") String description,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("r") String r,
                                      @RequestParam("s") String s) {

        SignatureData signature = new SignatureData(r, s);
        NewDocumentData documentData = new NewDocumentData(userHash, disputeHash, itemId, name, description, signature);
        NewDocumentCrypto documentCrypto = new NewDocumentCrypto();
        documentCrypto.signMessage(documentData);

        if ( !documentCrypto.verifySignature(documentData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        return documentService.newDocument(userHash, disputeHash, itemId, name, description, file);
    }

    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public void getDocument(@Valid @RequestBody GetDocumentRequest request, HttpServletResponse response) throws IOException {

        GetDocumentCrypto disputeCrypto = new GetDocumentCrypto();
        disputeCrypto.signMessage(request);

        if ( !disputeCrypto.verifySignature(request) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
            documentService.getDocument(request.getUserHash(), request.getDisputeHash(), request.getItemId(), request.getDocumentHash(), response);
        }

        response.flushBuffer();
    }

    @RequestMapping(path = "/getFile", method = RequestMethod.POST)
    public void getDocumentFile(@Valid @RequestBody GetDocumentRequest request, HttpServletResponse response) throws IOException {

        GetDocumentCrypto disputeCrypto = new GetDocumentCrypto();
        disputeCrypto.signMessage(request);

        if ( !disputeCrypto.verifySignature(request) ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(UNAUTHORIZED);
        }
        else {
            documentService.getDocumentFile(request.getUserHash(), request.getDisputeHash(), request.getItemId(), request.getDocumentHash(), response);
        }

        response.flushBuffer();
    }


}
