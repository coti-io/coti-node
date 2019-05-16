package io.coti.basenode.controllers;

import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Slf4j
@RestController
public class TransactionBatchController {

    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;


  /*  @GetMapping(value = "/transaction_batch")
    public ResponseEntity<GetTransactionBatchResponse> getTransactionBatch(@RequestParam @Valid @NotNull Long starting_index) {
        return ResponseEntity.ok(transactionHelper.getTransactionBatch(starting_index));
    }*/

   /* @GetMapping(value = "/transaction_batch_stream")
    public StreamingResponseBody getTransactionBatchStream(@RequestParam @Valid @NotNull Long starting_index) {
        StreamingResponseBody responseBody = out -> {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            for (int i = 0; i < 2; i++) {
                log.info(new String(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse())));
                oos.writeObject(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse()));
                oos.flush();
                //     out.write(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse()));
                //     out.flush();
            }


        };

        return responseBody;
    }*/

    @GetMapping(value = "/transaction_batch")
    public void getTransactionBatch(@RequestParam @Valid @NotNull Long starting_index, HttpServletResponse response) throws IOException {
        transactionService.getTransactionBatch(starting_index, response);
    }
}
