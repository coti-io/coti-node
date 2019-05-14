package io.coti.basenode.controllers;

import com.google.gson.Gson;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.http.GetTransactionBatchStreamResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

@Slf4j
@RestController
public class TransactionBatchController {

    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;


    @GetMapping(value = "/transaction_batch")
    public ResponseEntity<GetTransactionBatchResponse> getTransactionBatch(@RequestParam @Valid @NotNull Long starting_index) {
        return ResponseEntity.ok(transactionHelper.getTransactionBatch(starting_index));
    }

    @GetMapping(value = "/transaction_batch_stream")
    public StreamingResponseBody getTransactionBatchStream(@RequestParam @Valid @NotNull Long starting_index) {
        StreamingResponseBody responseBody = out -> {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            for (int i = 0; i < 2; i++) {
                log.info(new String(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse())));
                oos.write(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse()));
                oos.flush();
                //     out.write(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse()));
                //     out.flush();
            }


        };

        return responseBody;
    }

    @GetMapping(value = "/transaction_batch_stream2")
    public void getTransactionBatchStream2(@RequestParam @Valid @NotNull Long starting_index, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        for (int i = 0; i < 2; i++) {
            out.print(new String(jacksonSerializer.serialize(new GetTransactionBatchStreamResponse())));
            out.flush();
        }

        return;
    }
}
